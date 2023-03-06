package org.n1.av2.backend.service.scan

import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.terminal.TERMINAL_CHAT
import org.springframework.stereotype.Service

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanInfoService(
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
    private val currentUserService: CurrentUserService,
    private val stompService: StompService,
    private val traverseNodeService: TraverseNodeService,
    private val userLinkEntityService: UserLinkEntityService,
    private val runService: RunService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun searchSiteByName(siteName: String) {
        val siteProperties = sitePropertiesEntityService.findByName(siteName)
        if (siteProperties == null) {
            stompService.replyMessage(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScans = createNodeScans(siteProperties.siteId)

        val user = currentUserService.user

        val runId = runService.createRun(siteProperties.siteId, nodeScans, user)

        data class ScanSiteResponse(val runId: String, val siteId: String)
        val response = ScanSiteResponse(runId, siteProperties.siteId)
        stompService.reply(ServerActions.SERVER_SITE_DISCOVERED, response)
        sendScanInfosOfPlayer() // to update the scans in the home screen
    }

    private fun createNodeScans(siteId: String): MutableMap<String, NodeScan> {
        val traverseNodes = traverseNodeService.createTraverseNodesWithDistance(siteId)
        return traverseNodes.map {
            val nodeStatus = when (it.value.distance) {
                1 -> NodeScanStatus.DISCOVERED_1
                else -> NodeScanStatus.UNDISCOVERED_0
            }
            it.key to NodeScan(status = nodeStatus, distance = it.value.distance)
        }.toMap().toMutableMap()
    }


    fun deleteScan(runId: String) {
        userLinkEntityService.deleteUserScan(runId)
        sendScanInfosOfPlayer()
    }

    fun sendScanInfosOfPlayer() {
        val userId = currentUserService.userId
        sendScanInfosOfPlayer(userId)
    }

    data class ScanInfo(
        val runId: String,
        val siteName: String,
        val siteId: String,
        val initiatorName: String,
        val nodes: String,
        val efficiency: String
    )

    fun sendScanInfosOfPlayer(userId: String) {
        val runLinks = userLinkEntityService.findAllByUserId(userId)
        val scans = runEntityService.getAll(runLinks)
        val scanItems = scans.map(::createScanInfo)
        stompService.reply(ServerActions.SERVER_RECEIVE_USER_SCANS, scanItems)
    }

    fun shareScan(runId: String, user: User) {
        if (userLinkEntityService.hasUserScan(user, runId)) {
            stompService.replyTerminalReceive("[info]${user.name}[/] already has this scan.")
            return
        }
        userLinkEntityService.createUserScan(runId, user)
        stompService.replyTerminalReceive("Shared scan with [info]${user.name}[/].")

        val myUserName = currentUserService.user.name

        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)

        stompService.replyMessage(NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteProperties.name}"))
        stompService.toUserAllConnections(user.id,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            StompService.TerminalReceive(TERMINAL_CHAT, arrayOf("[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]"))
        )

        sendScanInfosOfPlayer(user.id)
    }



    fun createScanInfo(run: Run): ScanInfo {
        val site = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodes = run.nodeScanById.filterValues { it.status != NodeScanStatus.UNDISCOVERED_0 }.size
        val nodesText = if (run.scanDuration != null) "${nodes}" else "${nodes}+"
        val userName = userEntityService.getById(run.initiatorId).name
        val efficiencyText = efficiencyText(run)
        return ScanInfo(run.runId, site.name, site.siteId, userName, nodesText, efficiencyText)
    }

    private fun efficiencyText(run: Run): String {
        if (run.scanEfficiency == null) {
            return "(scanning)"
        }
        return "${run.scanEfficiency}%"
    }

    fun updateScanInfoToPlayers(run: Run) {
        val scanInfo = createScanInfo(run)
        userLinkEntityService.getUsersOfScan(run.runId).forEach {
            stompService.reply(ServerActions.SERVER_UPDATE_SCAN_INFO, scanInfo)
        }
    }
}