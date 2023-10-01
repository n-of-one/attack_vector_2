package org.n1.av2.backend.service.layerhacking.app.auth

import org.n1.av2.backend.entity.ice.IcePasswordService
import org.n1.av2.backend.entity.ice.IceStatus
import org.n1.av2.backend.entity.ice.IceStatusRepo
import org.n1.av2.backend.entity.ice.determineIceType
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.ice.PasswordIceLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.service.layerhacking.app.AppService
import org.n1.av2.backend.service.layerhacking.ice.password.UserType
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
    private val iceStatusRepo: IceStatusRepo,
    private val time: TimeService,
    private val stompService: StompService,
    private val hackedUtil: HackedUtil,
    private val userIceHackingService: UserIceHackingService,
    private val currentUserService: CurrentUserService,
    private val icePasswordService: IcePasswordService,
    private val appService: AppService,
) {

    fun findOrCreateIceStatus(layer: PasswordIceLayer): IceStatus {
        return iceStatusRepo.findByLayerId(layer.id) ?: findOrCreateIceForPasswordIce(layer)
    }

    private fun findOrCreateIceForPasswordIce(layer: PasswordIceLayer): IceStatus {
        val id = createId("password", iceStatusRepo::findById)
        return createStatus(id, layer)
    }

    fun findOrCreateIceStatus(iceId: String, layer: Layer): IceStatus {
        return iceStatusRepo.findById(iceId).getOrElse { createStatus(iceId, layer) }
    }

    fun createStatus(iceId: String, layer: Layer): IceStatus {
        val iceStatus = IceStatus(iceId, layer.id, LinkedList(), 0, time.longAgo())
        iceStatusRepo.save(iceStatus)
        return iceStatus
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


    fun enter(iceId: String, userType: UserType) {
        val iceStatus = iceStatusRepo.findById(iceId).getOrElse { error("Ice not found for id: ${iceId}") }

        val layer = findLayer(iceStatus)
        val type = iceId.determineIceType()
        val showHint = showHint(iceStatus, layer)
        val hint = if (layer is PasswordIceLayer) layer.hint else null
        val iceEnter = IceAppEnter(type, layer.strength, hint, iceStatus.lockedUntil, iceStatus.hackerAttempts, showHint, layer.hacked)

        stompService.reply(ServerActions.SERVER_AUTH_ENTER, iceEnter)
        if (userType == UserType.HACKER) {
            userIceHackingService.enter(iceId)
        }
    }

    fun submitAttempt(iceId: String, password: String, userType: UserType) {
        val iceStatus = iceStatusRepo.findById(iceId).getOrElse { error("Ice not found for id: ${iceId}") }
        val layer = findLayer(iceStatus)

        if (userType == UserType.USER) {
            resolveForUser(password, iceStatus, layer)
        } else {
            resolveForHacker(password, iceStatus, layer)
        }
    }

    data class UiStateUpdate(
        var lockedUntil: ZonedDateTime,
        val attempts: List<String>,
        val showHint: Boolean,
        val hacked: Boolean,
    )

    private fun resolveForUser(password: String, iceStatus: IceStatus, layer: IceLayer) {
        if (checkPassword(password, layer, iceStatus.id)) {
            respondStatusHacked(iceStatus, layer)
            val newStatus = iceStatus.copy(authorized = iceStatus.authorized + currentUserService.userId)
            iceStatusRepo.save(newStatus)
            redirectToNextLayer(layer)

        } else {
            val timeOutSeconds = calculateTimeOutSeconds(iceStatus.attemptCount)
            val newStatus = iceStatus.copy(
                lockedUntil = time.now().plusSeconds(timeOutSeconds),
                attemptCount = iceStatus.attemptCount + 1
            )
            iceStatusRepo.save(newStatus)
            val showHint = showHint(newStatus, layer)
            val result = UiStateUpdate(newStatus.lockedUntil, iceStatus.hackerAttempts, showHint, layer.hacked)
            stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)
        }
    }

    private fun redirectToNextLayer(layer: IceLayer) {
        val nextLayerPath = appService.determineNextLayerPath(layer)

        class PasswordCorrect(val path: String?)
        stompService.reply(ServerActions.SERVER_AUTH_PASSWORD_CORRECT, PasswordCorrect(nextLayerPath))
    }


    private fun resolveForHacker(password: String, iceStatus: IceStatus, layer: IceLayer) {
        if (iceStatus.hackerAttempts.contains(password)) {
            resolveDuplicate(iceStatus, password, layer)
        }
        else if (checkPassword(password, layer, iceStatus.id)) {
            respondStatusHacked(iceStatus, layer)

            if (!layer.hacked) hackedUtil.iceHacked(iceStatus.layerId, 70)
            redirectToNextLayer(layer)
        }
        else {
            resolveHackAttemptFailed(iceStatus, password, layer)
        }
    }

    private fun checkPassword(password: String, layer: IceLayer, iceId: String ): Boolean {
        if (layer is PasswordIceLayer && password == layer.password) {
            return true
        }

        return icePasswordService.checkIcePassword(iceId, password)
    }

    private fun resolveDuplicate(iceStatus: IceStatus, password: String, layer: IceLayer) {
        stompService.replyNeutral("Password \"${password}\" already attempted, ignoring")

        val showHint = showHint(iceStatus, layer)
        val result = UiStateUpdate(iceStatus.lockedUntil, iceStatus.hackerAttempts, showHint, layer.hacked)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)
    }

    private fun respondStatusHacked(iceStatus: IceStatus, layer: IceLayer) {
        val showHint = showHint(iceStatus, layer)
        val result = UiStateUpdate(time.longAgo(), iceStatus.hackerAttempts, showHint, true)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)

    }

    private fun resolveHackAttemptFailed(iceStatus: IceStatus, password: String, layer: IceLayer) {
        val newAttempts = iceStatus.hackerAttempts.plus(password).sorted()
        val timeOutSeconds = calculateTimeOutSeconds(iceStatus.attemptCount)

        val newStatus = iceStatus.copy(
            hackerAttempts = newAttempts,
            attemptCount = iceStatus.attemptCount + 1,
            lockedUntil = time.now().plusSeconds(timeOutSeconds)
        )
        iceStatusRepo.save(newStatus)
        stompService.replyNeutral("Password incorrect: ${password}")
        val showHint = showHint(newStatus, layer)
        val result = UiStateUpdate(newStatus.lockedUntil, newStatus.hackerAttempts, showHint, layer.hacked)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_UPDATE, result)
    }

    private fun showHint(iceStatus: IceStatus, layer: IceLayer): Boolean {
        if (layer !is PasswordIceLayer) {
            return false
        }
        return (layer.hint.trim().isNotEmpty() && iceStatus.attemptCount >= 3)
    }

    private fun findLayer(iceStatus: IceStatus): IceLayer {
        return nodeEntityService.findLayer(iceStatus.layerId) as IceLayer
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
        val iceStatus = iceStatusRepo.findByLayerId(layerId) ?: return
        iceStatusRepo.delete(iceStatus)
    }

}
