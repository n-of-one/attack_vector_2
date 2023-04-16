package org.n1.av2.backend.service.layerhacking.ice.password

import org.n1.av2.backend.entity.ice.PasswordIceStatus
import org.n1.av2.backend.entity.ice.PasswordIceStatusRepo
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.IcePasswordLayer
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.util.createId
import java.time.ZonedDateTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

@org.springframework.stereotype.Service
class PasswordService(
    val nodeEntityService: NodeEntityService,
    val passwordIceStatusRepo: PasswordIceStatusRepo,
    val time: TimeService,
    val stompService: StompService,
    val hackedUtil: HackedUtil,
    ) {

    data class UiState(
        val layerId: String,
        var lockedUntil: ZonedDateTime,
        val attempts: MutableList<String>,
        val message: String?,
        val hacked: Boolean,
        val hint: String?
    ) {
        constructor(message: String?, hacked: Boolean, hint: String?, status: PasswordIceStatus) :
                this(status.layerId, status.lockedUntil, status.attempts, message, hacked, hint)
    }

    fun enter(iceId: String) {
        val iceStatus = passwordIceStatusRepo
            .findById(iceId)
            .getOrElse {
                stompService.replyMessage(NotyMessage(NotyType.FATAL, "Error", "No ice for ID: ${iceId}"))
                return
            }

        val layer = getLayer(iceStatus)
        val hintToDisplay = hintToDisplay(iceStatus, layer)
        val uiState = UiState(null, false, hintToDisplay, iceStatus)

        stompService.reply(ServerActions.SERVER_ENTER_ICE_PASSWORD, uiState)
    }

    class SubmitPassword(val iceId: String, val password: String)

    fun submitAttempt(command: SubmitPassword) {
        val iceStatus = passwordIceStatusRepo.findById(command.iceId).getOrElse {  error("Ice not found for id: ${command.iceId}") }
        val layer = getLayer(iceStatus)

        when {
            iceStatus.attempts.contains(command.password) -> resolveDuplicate(iceStatus, layer, command.password)
            command.password == layer.password -> resolveHacked(iceStatus, command.password)
            else -> resolveFailed(iceStatus, layer, command.password)
        }
    }


    private fun resolveDuplicate(iceStatus: PasswordIceStatus, layer: IcePasswordLayer, password: String) {
        val hintToDisplay = hintToDisplay(iceStatus, layer)

        val result = UiState("Password \"${password}\" already attempted, ignoring", false, hintToDisplay, iceStatus)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_ICE_PASSWORD_UPDATE, result)
    }


    private fun resolveHacked(iceStatus: PasswordIceStatus, password: String) {
        iceStatus.attempts.add(password)

        val result = UiState("Password accepted.", true, null, iceStatus)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_ICE_PASSWORD_UPDATE, result)

        hackedUtil.iceHacked(iceStatus.layerId, iceStatus.runId, 70)
    }

    private fun resolveFailed(iceStatus: PasswordIceStatus, layer: IcePasswordLayer, password: String) {
        iceStatus.attempts.add(password)
        iceStatus.attempts.sort()
        val timeOutSeconds = calculateTimeOutSeconds(iceStatus.attempts.size)
        iceStatus.lockedUntil = time.now().plusSeconds(timeOutSeconds)

        val hintToDisplay = hintToDisplay(iceStatus, layer)
        passwordIceStatusRepo.save(iceStatus)

        val result = UiState("Password incorrect: ${password}", false, hintToDisplay, iceStatus)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_ICE_PASSWORD_UPDATE, result)
    }


    private fun calculateTimeOutSeconds(totalAttempts: Int): Long {
        return when (totalAttempts) {
            0, 1, 2 -> 2L
            3, 4, 5 -> 5L
            6, 7, 8, 9, 10 -> 10L
            else -> 15L
        }
    }

    private fun hintToDisplay(iceStatus: PasswordIceStatus, layer: IcePasswordLayer): String? {
        return if (iceStatus.attempts.size > 0) layer.hint else null
    }

    private fun getLayer(iceStatus: PasswordIceStatus) : IcePasswordLayer {
        val node = nodeEntityService.getById(iceStatus.nodeId)
        val layer = node.getLayerById(iceStatus.layerId)
        if (layer !is IcePasswordLayer) error("Wrong layer type/data for layer: ${layer.id}")
        return layer
    }

    fun createStatus(layer: IcePasswordLayer, nodeId: String, runId: String): PasswordIceStatus {
        val id = createId("password", passwordIceStatusRepo::findById)
        val passwordStatus = PasswordIceStatus(id, runId, nodeId, layer.id, LinkedList(), time.now().minusSeconds(1))
        passwordIceStatusRepo.save(passwordStatus)
        return passwordStatus
    }

}