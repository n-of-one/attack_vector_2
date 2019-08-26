package org.n1.av2.backend.service.service.ice.password

import org.n1.av2.backend.model.db.run.IcePasswordStatus
import org.n1.av2.backend.model.db.service.IcePasswordService
import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.IcePasswordStatusRepo
import org.n1.av2.backend.repo.ServiceStatusRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.util.createId
import org.n1.av2.backend.util.nodeIdFromServiceId
import java.util.*


@org.springframework.stereotype.Service
class ServiceIcePassword(
        val nodeService: NodeService,
        val serviceStatusRepo: ServiceStatusRepo,
        val icePasswordStatusRepo: IcePasswordStatusRepo,
        val currentUser: CurrentUserService,
        val time: TimeService,
        val stompService: StompService) {

    data class UiState(val message: String?, val hacked: Boolean, val hint: String?, val status: IcePasswordStatus)

    fun hack(service: Service, runId: String) {
        val passwordStatus = getOrCreateStatus(service.id, runId)
        val passwordService = getService(service.id)
        val hintToDisplay = hintToDisplay(passwordStatus, passwordService)

        val uiState = UiState(null, false, hintToDisplay, passwordStatus)

        stompService.toRun(runId, ReduxActions.SERVER_START_HACKING_ICE_PASSWORD, uiState)
    }


    private fun getOrCreateStatus(serviceId: String, runId: String): IcePasswordStatus {

        return icePasswordStatusRepo.findByServiceIdAndRunId(serviceId, runId) ?: createServiceStatus(serviceId, runId)
    }

    private fun createServiceStatus(serviceId: String, runId: String): IcePasswordStatus {
        val id = createId("icePasswordStatus-")
        val passwordStatus = IcePasswordStatus(id, serviceId, runId, LinkedList<String>(), time.now().minusSeconds(1))
        icePasswordStatusRepo.save(passwordStatus)
        return passwordStatus
    }


    class SubmitPassword(val serviceId: String, val runId: String, val password: String)

    fun submitAttempt(command: SubmitPassword) {
        val status = getOrCreateStatus(command.serviceId, command.runId)
        val service = getService(command.serviceId)


        when {
            status.attempts.contains(command.password) -> resolveDuplicate(command.runId, status, command.password, service.hint)
            command.password == service.password -> resolveHacked(command, command.password)
            else -> resolveFailed(command.runId, status, command.password, service.hint)
        }
    }

    private fun getService(serviceId: String): IcePasswordService {
        val nodeId = nodeIdFromServiceId(serviceId)
        val node = nodeService.getById(nodeId)
        return node.getServiceById(serviceId) as IcePasswordService
    }


    private fun resolveDuplicate(runId: String, status: IcePasswordStatus, password: String, hint: String) {
        val hintToDisplay = if (status.attempts.size > 0) hint else null
        val result = UiState("Password \"${password}\" already attempted, ignoring", false, hintToDisplay, status)
        stompService.toRun(runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
    }


    private fun resolveHacked(command: SubmitPassword, password: String) {
        val layerStatus = serviceStatusRepo.findByServiceIdAndRunId(command.serviceId, command.runId) !!
        val passwordStatus = getOrCreateStatus(command.serviceId, command.runId)
        passwordStatus.attempts.add(password)
        layerStatus.hackedBy.add(currentUser.userId)
        layerStatus.hacked = true
        serviceStatusRepo.save(layerStatus)

        val result = UiState("Password accepted.", true, null, passwordStatus)
        stompService.toRun(command.runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
        stompService.toRun(command.runId, ReduxActions.SERVER_ICE_HACKED, layerStatus.serviceId)
    }

    private fun resolveFailed(runId: String, status: IcePasswordStatus, password: String, hint: String) {
        status.attempts.add(password)
        status.attempts.sort()
        val timeOutSeconds = calculateTimeOutSeconds(status.attempts.size)
        status.lockedUntil = time.now().plusSeconds(timeOutSeconds)

        val hintToDisplay = if (status.attempts.size > 0) hint else null
        icePasswordStatusRepo.save(status)

        val result = UiState("Password incorrect, time-out: ${timeOutSeconds} seconds.", false, hintToDisplay, status)
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

    private fun hintToDisplay(status: IcePasswordStatus, service: IcePasswordService): String? {
        return if (status.attempts.size > 0) service.hint else null
    }


}