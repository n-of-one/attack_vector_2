package org.n1.av2.backend.service.layerhacking.app.auth

import org.n1.av2.backend.service.layerhacking.service.KeystoreService
import org.n1.av2.backend.entity.ice.IcePasswordStatus
import org.n1.av2.backend.entity.ice.IcePasswordStatusRepo
import org.n1.av2.backend.entity.ice.determineIceType
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.ice.PasswordIceLayer
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.service.layerhacking.app.AppService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.user.UserIceHackingService
import org.n1.av2.backend.util.createId
import java.time.ZonedDateTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

/**
 * Every implementation of ICE has an IceStatus. It represents the status of the ICE when users interact with it
 * through the IceApp. Anyone can enter the password if they know it, bypassing the ICE.
 *
 * Most ICE does not have a static password, so a password can only be retrieved from a keystore
 *
 * Password ICE is special in that it has a static password
 */

@org.springframework.stereotype.Service
class AuthAppService(
    private val nodeEntityService: NodeEntityService,
    private val icePasswordStatusRepo: IcePasswordStatusRepo,
    private val time: TimeService,
    private val stompService: StompService,
    private val hackedUtil: HackedUtil,
    private val userIceHackingService: UserIceHackingService,
    private val currentUser: CurrentUserService,
    private val keystoreService: KeystoreService,
    private val appService: AppService,
) {

    fun findOrCreateIceStatus(layer: PasswordIceLayer): IcePasswordStatus {
        return icePasswordStatusRepo.findByLayerId(layer.id) ?: findOrCreateIceForPasswordIce(layer)
    }

    private fun findOrCreateIceForPasswordIce(layer: PasswordIceLayer): IcePasswordStatus {
        val id = createId("password", icePasswordStatusRepo::findById)
        return createStatus(id, layer)
    }

    fun findOrCreateIceStatus(iceId: String, layer: Layer): IcePasswordStatus {
        return icePasswordStatusRepo.findById(iceId).getOrElse { createStatus(iceId, layer) }
    }

    fun createStatus(iceId: String, layer: Layer): IcePasswordStatus {
        val icePasswordStatus = IcePasswordStatus(iceId, layer.id, LinkedList(), 0, time.longAgo())
        icePasswordStatusRepo.save(icePasswordStatus)
        return icePasswordStatus
    }

    data class IceAppEnter(
        val type: LayerType,
        val strength: IceStrength,
        val hint: String?,

        val lockedUntil: ZonedDateTime,
        val attempts: List<String>,
        val showHint: Boolean,
        val hacked: Boolean,
    )


    fun enter(iceId: String) {
        val iceStatus = icePasswordStatusRepo.findById(iceId).getOrElse { error("Ice not found for id: ${iceId}") }

        val layer = findLayer(iceStatus)
        val type = iceId.determineIceType()
        val showHint = showHint(iceStatus, layer)
        val hint = if (layer is PasswordIceLayer) layer.hint else null
        val iceEnter = IceAppEnter(type, layer.strength, hint, iceStatus.lockedUntil, iceStatus.hackerAttempts, showHint, layer.hacked)

        stompService.reply(ServerActions.SERVER_AUTH_ENTER, iceEnter)

        if (currentUser.userEntity.type == UserType.HACKER) {
            userIceHackingService.enter(iceId)
        }
    }

    fun submitAttempt(iceId: String, password: String) {
        val iceStatus = icePasswordStatusRepo.findById(iceId).getOrElse { error("Ice not found for id: ${iceId}") }
        val layer = findLayer(iceStatus)

        if (currentUser.userEntity.type == UserType.HACKER) {
            resolveForHacker(password, iceStatus, layer)
        }
        else {
            resolveForUser(password, iceStatus, layer)
        }
    }

    data class UiStateUpdate(
        var lockedUntil: ZonedDateTime,
        val attempts: List<String>,
        val showHint: Boolean,
        val hacked: Boolean,
    )

    private fun resolveForUser(password: String, icePasswordStatus: IcePasswordStatus, layer: IceLayer) {
        if (checkPassword(password, layer, icePasswordStatus.id)) {
            respondStatusHacked(icePasswordStatus, layer)
            val newStatus = icePasswordStatus.copy(authorized = icePasswordStatus.authorized + currentUser.userId)
            icePasswordStatusRepo.save(newStatus)
            redirectToNextLayer(layer)

        } else {
            val timeOutSeconds = calculateTimeOutSeconds(icePasswordStatus.attemptCount)
            val newStatus = icePasswordStatus.copy(
                lockedUntil = time.now().plusSeconds(timeOutSeconds),
                attemptCount = icePasswordStatus.attemptCount + 1
            )
            icePasswordStatusRepo.save(newStatus)
            val showHint = showHint(newStatus, layer)
            val result = UiStateUpdate(newStatus.lockedUntil, icePasswordStatus.hackerAttempts, showHint, layer.hacked)
            stompService.toIce(icePasswordStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)
        }
    }

    private fun redirectToNextLayer(layer: IceLayer) {
        val nextLayerPath = appService.determineNextLayerPath(layer)

        class PasswordCorrect(val path: String?)
        stompService.reply(ServerActions.SERVER_AUTH_PASSWORD_CORRECT, PasswordCorrect(nextLayerPath))
    }


    private fun resolveForHacker(password: String, icePasswordStatus: IcePasswordStatus, layer: IceLayer) {
        if (icePasswordStatus.hackerAttempts.contains(password)) {
            resolveDuplicate(icePasswordStatus, password, layer)
        }
        else if (checkPassword(password, layer, icePasswordStatus.id)) {
            respondStatusHacked(icePasswordStatus, layer)

            if (!layer.hacked) hackedUtil.iceHacked(icePasswordStatus.layerId, 70)
            redirectToNextLayer(layer)
        }
        else {
            resolveHackAttemptFailed(icePasswordStatus, password, layer)
        }
    }

    private fun checkPassword(password: String, layer: IceLayer, iceId: String ): Boolean {
        if (layer is PasswordIceLayer && password == layer.password) {
            return true
        }

        return keystoreService.checkIcePassword(iceId, password)
    }

    private fun resolveDuplicate(icePasswordStatus: IcePasswordStatus, password: String, layer: IceLayer) {
        stompService.replyNeutral("Password \"${password}\" already attempted, ignoring")

        val showHint = showHint(icePasswordStatus, layer)
        val result = UiStateUpdate(icePasswordStatus.lockedUntil, icePasswordStatus.hackerAttempts, showHint, layer.hacked)
        stompService.toIce(icePasswordStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)
    }

    private fun respondStatusHacked(icePasswordStatus: IcePasswordStatus, layer: IceLayer) {
        val showHint = showHint(icePasswordStatus, layer)
        val result = UiStateUpdate(time.longAgo(), icePasswordStatus.hackerAttempts, showHint, true)
        stompService.toIce(icePasswordStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)

    }

    private fun resolveHackAttemptFailed(icePasswordStatus: IcePasswordStatus, password: String, layer: IceLayer) {
        val newAttempts = icePasswordStatus.hackerAttempts.plus(password).sorted()
        val timeOutSeconds = calculateTimeOutSeconds(icePasswordStatus.attemptCount)

        val newStatus = icePasswordStatus.copy(
            hackerAttempts = newAttempts,
            attemptCount = icePasswordStatus.attemptCount + 1,
            lockedUntil = time.now().plusSeconds(timeOutSeconds)
        )
        icePasswordStatusRepo.save(newStatus)
        stompService.replyNeutral("Password incorrect: ${password}")
        val showHint = showHint(newStatus, layer)
        val result = UiStateUpdate(newStatus.lockedUntil, newStatus.hackerAttempts, showHint, layer.hacked)
        stompService.toIce(icePasswordStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)
    }

    private fun showHint(icePasswordStatus: IcePasswordStatus, layer: IceLayer): Boolean {
        if (layer !is PasswordIceLayer) {
            return false
        }
        return (layer.hint.trim().isNotEmpty() && icePasswordStatus.attemptCount >= 3)
    }

    private fun findLayer(icePasswordStatus: IcePasswordStatus): IceLayer {
        return nodeEntityService.findLayer(icePasswordStatus.layerId) as IceLayer
    }


    private fun calculateTimeOutSeconds(totalAttempts: Int): Long {
        return when (totalAttempts) {
            0, 1, 2 -> 2L
            3, 4, 5 -> 5L
            6, 7, 8, 9, 10 -> 10L
            else -> 15L
        }
    }


    fun deleteByLayerId(layerId: String) {
        val iceStatus = icePasswordStatusRepo.findByLayerId(layerId) ?: return
        icePasswordStatusRepo.delete(iceStatus)
    }

}
