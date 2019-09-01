package org.n1.av2.backend.service.service.ice.password

import org.n1.av2.backend.model.db.layer.IcePasswordLayer
import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.IcePasswordStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IcePasswordStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.service.HackedUtil
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.nodeIdFromServiceId
import java.time.ZonedDateTime
import java.util.*

@org.springframework.stereotype.Service
class IcePasswordService(
        val nodeService: NodeService,
        val icePasswordStatusRepo: IcePasswordStatusRepo,
        val time: TimeService,
        val serviceIceUtil: HackedUtil,
        val stompService: StompService) {


    data class UiState(val message: String?,
                       val hacked: Boolean,
                       val hint: String?,
                       val layerId: String,
                       val attempts: MutableList<String>,
                       var lockedUntil: ZonedDateTime) {

        constructor(message: String?, hacked: Boolean, hint: String?, status: IcePasswordStatus ) :
                this(message, hacked, hint, status.layerId, status.attempts, status.lockedUntil)
    }

    fun hack(layer: Layer, runId: String) {
        val passwordStatus = getOrCreateStatus(layer.id, runId)

        val nodeId = nodeIdFromServiceId(layer.id)
        val node = nodeService.getById(nodeId)
        val passwordService =  node.getLayerById(layer.id) as IcePasswordLayer

        val hintToDisplay = hintToDisplay(passwordStatus, passwordService)

        val uiState = UiState(null, false, hintToDisplay, passwordStatus)

        stompService.toUser(ReduxActions.SERVER_START_HACKING_ICE_PASSWORD, uiState)
    }


    private fun getOrCreateStatus(layerId: String, runId: String): IcePasswordStatus {
        return icePasswordStatusRepo.findByLayerIdAndRunId(layerId, runId) ?: createServiceStatus(layerId, runId)
    }

    private fun createServiceStatus(layerId: String, runId: String): IcePasswordStatus {
        val id = createId("icePasswordStatus-")
        val passwordStatus = IcePasswordStatus(id, layerId, runId, LinkedList<String>(), time.now().minusSeconds(1))
        icePasswordStatusRepo.save(passwordStatus)
        return passwordStatus
    }


    class SubmitPassword(val layerId: String, val runId: String, val password: String)

    fun submitAttempt(command: SubmitPassword) {
        val status = getOrCreateStatus(command.layerId, command.runId)
        val nodeId = nodeIdFromServiceId(command.layerId)
        val node = nodeService.getById(nodeId)
        val layer =  node.getLayerById(command.layerId) as IcePasswordLayer

        when {
            status.attempts.contains(command.password) -> resolveDuplicate(command.runId, status, command.password, layer.hint)
            command.password == layer.password -> resolveHacked(command, command.password, node)
            else -> resolveFailed(command.runId, status, command.password, layer.hint)
        }
    }



    private fun resolveDuplicate(runId: String, status: IcePasswordStatus, password: String, hint: String) {
        val hintToDisplay = if (status.attempts.size > 0) hint else null
        val result = UiState("Password \"${password}\" already attempted, ignoring", false, hintToDisplay, status)
        stompService.toRun(runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
    }



    private fun resolveHacked(command: SubmitPassword, password: String, node: Node) {
        val passwordStatus = getOrCreateStatus(command.layerId, command.runId)
        passwordStatus.attempts.add(password)


        val result = UiState("Password accepted.", true, null, passwordStatus)
        stompService.toRun(command.runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
        serviceIceUtil.iceHacked(command.layerId, node, command.runId, 70)
    }

    private fun resolveFailed(runId: String, status: IcePasswordStatus, password: String, hint: String) {
        status.attempts.add(password)
        status.attempts.sort()
        val timeOutSeconds = calculateTimeOutSeconds(status.attempts.size)
        status.lockedUntil = time.now().plusSeconds(timeOutSeconds)

        val hintToDisplay = if (status.attempts.size > 0) hint else null
        icePasswordStatusRepo.save(status)

        val result = UiState("Password incorrect: ${password}", false, hintToDisplay, status)
        stompService.toRun(runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
    }


    private fun calculateTimeOutSeconds(totalAttempts: Int): Long {
        return when (totalAttempts) {
            0, 1, 2 -> 2L
            3, 4, 5 -> 5L
            6, 7, 8, 9, 10 -> 10L
            else -> 15L
        }
    }

    private fun hintToDisplay(status: IcePasswordStatus, layer: IcePasswordLayer): String? {
        return if (status.attempts.size > 0) layer.hint else null
    }


}