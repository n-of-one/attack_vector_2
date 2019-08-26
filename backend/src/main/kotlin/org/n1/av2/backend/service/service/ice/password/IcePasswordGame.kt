package org.n1.av2.backend.service.service.ice.password

import org.n1.av2.backend.model.db.run.IcePasswordStatus
import org.n1.av2.backend.model.db.run.ServiceStatusHolder
import org.n1.av2.backend.model.db.service.IcePasswordService
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.repo.ServiceStatusHolderRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.util.nodeIdFromServiceId
import org.springframework.data.repository.findByIdOrNull
import java.util.*


@org.springframework.stereotype.Service
class IcePasswordGame(
        val nodeService: NodeService,
        val serviceStatusRepo:
        ServiceStatusHolderRepo,
        val currentUser: CurrentUserService,
        val time: TimeService,
        val stompService: StompService) {

    fun getOrCreateStatusHolder(serviceId: String, runId: String): ServiceStatusHolder {
        return serviceStatusRepo.findByIdOrNull(serviceId) ?: createStatusHolder(serviceId, runId)
    }

    private fun createStatusHolder(serviceId: String, runId: String): ServiceStatusHolder {
        val status = IcePasswordStatus(LinkedList(), null)
        val holder = ServiceStatusHolder(serviceId, runId, false, LinkedList(), status)
        serviceStatusRepo.save(holder)
        return holder
    }


    class SubmitPassword(val serviceId: String, val runId: String, val password: String)

    fun submit(command: SubmitPassword) {
        val statusHolder = getOrCreateStatusHolder(command.serviceId, command.runId)
        val nodeId = nodeIdFromServiceId(command.serviceId)
        val node = nodeService.getById(nodeId)
        val service = node.getServiceById(command.serviceId) as IcePasswordService

        if (command.password == service.password) {
            resolveHacked(statusHolder, command.password)
        } else {
            resolveFailed(statusHolder, command.password)
        }
    }

    data class ResultIcePasswordAttempt(val message: String, val hacked: Boolean, val status: ServiceStatusHolder)

    private fun resolveHacked(statusHolder: ServiceStatusHolder, password: String) {
        val status = statusHolder.status as IcePasswordStatus
        status.attempts.add(password)
        statusHolder.hackedBy.add(currentUser.userId)
        statusHolder.hacked = true
        serviceStatusRepo.save(statusHolder)

        val result = ResultIcePasswordAttempt("Password accepted.", true, statusHolder)
        stompService.toRun(statusHolder.runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
        stompService.toRun(statusHolder.runId, ReduxActions.SERVER_ICE_HACKED, statusHolder.serviceId)
    }

    private fun resolveFailed(statusHolder: ServiceStatusHolder, password: String) {
        val status = statusHolder.status as IcePasswordStatus
        status.attempts.add(password)
        val timeOutSeconds = calculateTimeOutSeconds(status.attempts.size)
        status.lockedUntil = time.now().plusSeconds(timeOutSeconds)


        if (status.attempts.size == 1 ) {
            status.displayHint = true
        }
        serviceStatusRepo.save(statusHolder)

        val result = ResultIcePasswordAttempt("Password incorrect, try again in ${timeOutSeconds} seconds.", false, statusHolder)
        stompService.toRun(statusHolder.runId, ReduxActions.SERVER_ICE_PASSWORD_UPDATE, result)
    }


    private fun calculateTimeOutSeconds(totalAttempts: Int): Long {
        return when (totalAttempts) {
            0, 1, 2 -> 2L
            3, 4, 5 -> 5L
            else -> 10L
        }
    }


}