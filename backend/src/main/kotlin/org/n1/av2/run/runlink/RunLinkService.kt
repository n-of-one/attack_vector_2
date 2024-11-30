package org.n1.av2.run.runlink

import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions.*
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.run.entity.NodeScanStatus
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

@Service
class RunLinkService(
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val currentUserService: CurrentUserService,
    private val connectionService: ConnectionService,
    private val runLinkEntityService: RunLinkEntityService,
) {

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
        connectionService.toUser(userId, SERVER_UPDATE_USER_RUNS, scanItems)
    }

    fun shareRun(runId: String, userEntity: UserEntity, sendFeedback: Boolean) {
        if (runLinkEntityService.hasUserRunLink(userEntity, runId)) {
            if (sendFeedback) connectionService.replyTerminalReceive("[info]${userEntity.name}[/] already has this run.")
            return
        }
        runLinkEntityService.createRunLink(runId, userEntity)
        if (sendFeedback) connectionService.replyTerminalReceive("Shared run with [info]${userEntity.name}[/].")

        val myUserName = currentUserService.userEntity.name

        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)

        if (sendFeedback) {
            connectionService.toUser(userEntity.id, SERVER_NOTIFICATION, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteProperties.name}"))
            connectionService.toUser(
                userEntity.id,
                SERVER_TERMINAL_RECEIVE,
                ConnectionService.TerminalReceive(TERMINAL_MAIN, arrayOf("[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]"))
            )

            sendRunInfosToUser(userEntity.id)
        }
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
            connectionService.toUser(userId, SERVER_UPDATE_RUN_INFO, runInfo)
        }
    }

}
