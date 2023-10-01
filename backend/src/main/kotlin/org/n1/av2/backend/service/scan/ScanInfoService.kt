package org.n1.av2.backend.service.scan

import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.terminal.TERMINAL_CHAT
import org.n1.av2.backend.service.user.CurrentUserService
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
    private val userRunLinkEntityService: UserRunLinkEntityService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun createNodeScans(siteId: String): MutableMap<String, NodeScan> {
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
        userRunLinkEntityService.deleteUserScan(runId)
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
        val runLinks = userRunLinkEntityService.findAllByUserId(userId)
        val scans = runEntityService.getAll(runLinks)
        val scanItems = scans.map(::createScanInfo)
        stompService.toUser(userId, ServerActions.SERVER_UPDATE_USER_SCANS, scanItems)
    }

    fun shareScan(runId: String, userEntity: UserEntity) {
        if (userRunLinkEntityService.hasUserScan(userEntity, runId)) {
            stompService.replyTerminalReceive("[info]${userEntity.name}[/] already has this scan.")
            return
        }
        userRunLinkEntityService.createUserScan(runId, userEntity)
        stompService.replyTerminalReceive("Shared scan with [info]${userEntity.name}[/].")

        val myUserName = currentUserService.userEntity.name

        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)

        stompService.replyMessage(NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteProperties.name}"))
        stompService.toUser(userEntity.id,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            StompService.TerminalReceive(TERMINAL_CHAT, arrayOf("[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]"))
        )

        sendScanInfosOfPlayer(userEntity.id)
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
        userRunLinkEntityService.getUsersOfScan(run.runId).forEach {
            stompService.reply(ServerActions.SERVER_UPDATE_SCAN_INFO, scanInfo)
        }
    }
}