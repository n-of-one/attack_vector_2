package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.TERMINAL_CHAT
import org.n1.av2.backend.service.run.outside.scanning.TraverseNodeService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

/** This service is about ScanInfo: . */
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

    fun sendScanInfosOfPlayer(userId: String) {
        val runLinks = userRunLinkEntityService.findAllByUserId(userId)
        val runs = runEntityService.getAll(runLinks)
        val scanItems = runs.map(::createScanInfo)
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
        stompService.toUser(
            userEntity.id,
            ServerActions.SERVER_TERMINAL_RECEIVE,
            StompService.TerminalReceive(TERMINAL_CHAT, arrayOf("[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]"))
        )

        sendScanInfosOfPlayer(userEntity.id)
    }

    data class ScanInfo(val runId: String, val siteName: String, val siteId: String, val nodes: String)
    fun createScanInfo(run: Run): ScanInfo {
        val site = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodesCount = run.nodeScanById.filterValues { it.status != NodeScanStatus.UNDISCOVERED_0 }.size
        return ScanInfo(run.runId, site.name, site.siteId, "${nodesCount}+")
    }

    fun updateScanInfoToPlayers(run: Run) {
        val scanInfo = createScanInfo(run)
        userRunLinkEntityService.getUsersOfScan(run.runId).forEach {
            stompService.reply(ServerActions.SERVER_UPDATE_SCAN_INFO, scanInfo)
        }
    }
}