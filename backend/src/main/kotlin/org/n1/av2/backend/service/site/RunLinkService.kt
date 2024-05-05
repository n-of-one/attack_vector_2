package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.run.RunLinkEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions.*
import org.n1.av2.backend.service.run.TERMINAL_CHAT
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

/** This service is about ScanInfo: . */
@Service
class RunLinkService(
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val currentUserService: CurrentUserService,
    private val stompService: StompService,
    private val runLinkEntityService: RunLinkEntityService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun deleteRunLink(runId: String) {
        runLinkEntityService.deleteRunLink(runId)
        sendRunInfosToUser()
    }

    fun sendRunInfosToUser() {
        val userId = currentUserService.userId
        sendRunInfosToUser(userId)
    }

    fun sendRunInfosToUser(userId: String) {
        val runLinks = runLinkEntityService.findAllByUserId(userId)
        val runs = runEntityService.getAll(runLinks)
        val scanItems = runs.map(::createRunInfo)
        stompService.toUser(userId, SERVER_UPDATE_USER_RUNS, scanItems)
    }

    fun shareRun(runId: String, userEntity: UserEntity, sendFeedback: Boolean) {
        if (runLinkEntityService.hasUserRunLink(userEntity, runId)) {
            if (sendFeedback) stompService.replyTerminalReceive("[info]${userEntity.name}[/] already has this run.")
            return
        }
        runLinkEntityService.createRunLink(runId, userEntity)
        if (sendFeedback) stompService.replyTerminalReceive("Shared run with [info]${userEntity.name}[/].")

        val myUserName = currentUserService.userEntity.name

        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)

        stompService.toUser(userEntity.id, SERVER_NOTIFICATION, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteProperties.name}"))
        stompService.toUser(
            userEntity.id,
            SERVER_TERMINAL_RECEIVE,
            StompService.TerminalReceive(TERMINAL_CHAT, arrayOf("[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]"))
        )

        sendRunInfosToUser(userEntity.id)
    }

    data class RunInfo(val runId: String, val siteName: String, val siteId: String, val nodes: String)
    fun createRunInfo(run: Run): RunInfo {
        val site = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodesCount = run.nodeScanById.filterValues { it.status != NodeScanStatus.UNDISCOVERED_0 }.size
        val siteComplete = nodesCount == run.nodeScanById.size
        val suffix = if (siteComplete) "" else "+"
        return RunInfo(run.runId, site.name, site.siteId, "${nodesCount}${suffix}")
    }

    fun sendUpdatedRunInfoToHackers(run: Run) {
        val runInfo = createRunInfo(run)
        runLinkEntityService.getUserIdsOfRun(run.runId).forEach {userId ->
            stompService.toUser(userId, SERVER_UPDATE_RUN_INFO, runInfo)
        }
    }

}
