package org.n1.av2.backend.service.layerhacking.app.auth

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
import org.n1.av2.backend.service.layerhacking.ice.password.UserType
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.user.UserIceHackingService
import org.n1.av2.backend.util.createId
import java.time.ZonedDateTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

/**
 * Every implementation of ICE has an IceStatus. It represents the status of the ICE when users interact with it
 * through the IceApp.
 *
 * Password ICE is a bit special, as it ONLY has an IceStatus, but it also has a fixed password and optional password hint.
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

    private fun createStatus(iceId: String, layer: Layer): IceStatus {
        val passwordStatus = IceStatus(iceId, layer.id, LinkedList(), 0, time.longAgo())
        iceStatusRepo.save(passwordStatus)
        return passwordStatus
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
        val iceStatus = iceStatusRepo.findById(iceId).getOrElse { error("No Password ice for ID: ${iceId}") }

        val layer = getLayer(iceStatus)
        val type = iceId.determineIceType()
        val showHint = showHint(iceStatus)
        val hint = if (layer is PasswordIceLayer) layer.hint else null
        val iceEnter = IceAppEnter(type, layer.strength, hint, iceStatus.lockedUntil, iceStatus.hackerAttempts, showHint, iceStatus.hacked)

        stompService.reply(ServerActions.SERVER_AUTH_APP_ENTER, iceEnter)
        if (userType == UserType.HACKER) {
            userIceHackingService.enter(iceId)
        }
    }

    fun submitAttempt(iceId: String, password: String, userType: UserType) {
        val iceStatus = iceStatusRepo.findById(iceId).getOrElse { error("Ice not found for id: ${iceId}") }
        val layer = getLayer(iceStatus)

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
        if (layer is PasswordIceLayer && layer.password == password) {
            val newStatus = iceStatus.copy(authorized = iceStatus.authorized + currentUserService.userId)
            iceStatusRepo.save(newStatus)
            stompService.reply(ServerActions.SERVER_AUTH_APP_PASSWORD_CORRECT, "")

        } else {
            val timeOutSeconds = calculateTimeOutSeconds(iceStatus.attemptCount)
            val newStatus = iceStatus.copy(
                lockedUntil = time.now().plusSeconds(timeOutSeconds),
                attemptCount = iceStatus.attemptCount + 1
            )
            iceStatusRepo.save(newStatus)
            val showHint = showHint(newStatus)
            val result = UiStateUpdate(newStatus.lockedUntil, iceStatus.hackerAttempts, showHint, iceStatus.hacked)
            stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_APP_UPDATE, result)
        }
    }


    private fun resolveForHacker(password: String, iceStatus: IceStatus, layer: IceLayer) {
        if (iceStatus.hackerAttempts.contains(password)) {
            resolveDuplicate(iceStatus, password)
        }
        else if (layer is PasswordIceLayer && password == layer.password) {
            resolveHacked(iceStatus, password)
        }
        else {
            resolveHackAttemptFailed(iceStatus, password)
        }

    }

    private fun resolveDuplicate(iceStatus: IceStatus, password: String) {
        stompService.replyNeutral("Password \"${password}\" already attempted, ignoring")

        val showHint = showHint(iceStatus)
        val result = UiStateUpdate(iceStatus.lockedUntil, iceStatus.hackerAttempts, showHint, iceStatus.hacked)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_APP_UPDATE, result)
    }

    private fun resolveHacked(iceStatus: IceStatus, password: String) {
        val newStatus = iceStatus.copy(hacked = true)
        iceStatusRepo.save(newStatus)

        stompService.replyNeutral("Password accepted: ${password}")

        val showHint = showHint(newStatus)
        val result = UiStateUpdate(time.longAgo(), iceStatus.hackerAttempts, showHint, true)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_APP_UPDATE, result)

        if (!iceStatus.hacked) hackedUtil.iceHacked(iceStatus.layerId, 70)
    }

    private fun resolveHackAttemptFailed(iceStatus: IceStatus, password: String) {
        val newAttempts = iceStatus.hackerAttempts.plus(password).sorted()
        val timeOutSeconds = calculateTimeOutSeconds(iceStatus.attemptCount)

        val newStatus = iceStatus.copy(
            hackerAttempts = newAttempts,
            attemptCount = iceStatus.attemptCount + 1,
            lockedUntil = time.now().plusSeconds(timeOutSeconds)
        )
        iceStatusRepo.save(newStatus)
        stompService.replyNeutral("Password incorrect: ${password}")
        val showHint = showHint(newStatus)
        val result = UiStateUpdate(newStatus.lockedUntil, newStatus.hackerAttempts, showHint, newStatus.hacked)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_AUTH_APP_UPDATE, result)
    }

    private fun showHint(iceStatus: IceStatus): Boolean {
        val iceType = iceStatus.id.determineIceType()
        if (iceType != LayerType.PASSWORD_ICE) {
            return false
        }
        val layer = getLayer(iceStatus) as PasswordIceLayer
        return (layer.hint.trim().isNotEmpty() && iceStatus.attemptCount >= 3)
    }


    private fun calculateTimeOutSeconds(totalAttempts: Int): Long {
        return when (totalAttempts) {
            0, 1, 2 -> 2L
            3, 4, 5 -> 5L
            6, 7, 8, 9, 10 -> 10L
            else -> 15L
        }
    }

    private fun getLayer(iceStatus: IceStatus): IceLayer {
        val node = nodeEntityService.findByLayerId(iceStatus.layerId)
        val layer = node.getLayerById(iceStatus.layerId)
        if (layer !is IceLayer) error("Wrong layer type/data for layer: ${layer.id}")
        return layer
    }


    fun deleteByLayerId(layerId: String) {
        val iceStatus = iceStatusRepo.findByLayerId(layerId) ?: return
        iceStatusRepo.delete(iceStatus)
    }
}
