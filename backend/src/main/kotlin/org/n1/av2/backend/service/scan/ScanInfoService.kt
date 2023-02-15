package org.n1.av2.backend.service.scan

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanInfoService(private val runEntityService: RunEntityService,
                      private val sitePropertiesEntityService: SitePropertiesEntityService,
                      private val userEntityService: UserEntityService,
                      private val hackerStateEntityService: HackerStateEntityService,
                      private val currentUserService: CurrentUserService,
                      private val stompService: StompService,
) {

    private val logger = mu.KotlinLogging.logger {}


    fun deleteScan(runId: String) {
        runEntityService.deleteUserScan(runId)
        sendScanInfosOfPlayer()
    }

    fun sendScanInfosOfPlayer() {
        val userId = currentUserService.userId
        sendScanInfosOfPlayer(userId)
    }

    data class ScanInfo(val runId: String,
                        val siteName: String,
                        val siteId: String,
                        val initiatorName: String,
                        val nodes: String,
                        val efficiency: String)

    fun sendScanInfosOfPlayer(userId: String) {
        val scans = runEntityService.getAll(userId)
        val scanItems = scans.map (::createScanInfo)
        stompService.toUser(userId, ReduxActions.SERVER_RECEIVE_USER_SCANS, scanItems)
    }

    fun shareScan(runId: String, user: User) {
        if (runEntityService.hasUserScan(user, runId)) {
            stompService.terminalReceiveCurrentUser("[info]${user.name}[/] already has this scan.")
            return
        }
        runEntityService.createUserScan(runId, user)
        stompService.terminalReceiveCurrentUser("Shared scan with [info]${user.name}[/].")

        val myUserName = currentUserService.user.name

        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)

        stompService.toUser(user.id, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteProperties.name}"))
        stompService.terminalReceiveForUserForTerminal(user.id, "chat", "[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]")
        sendScanInfosOfPlayer(user.id)
    }

    fun leaveRun(runId: String) {
        hackerStateEntityService.leaveRun()
    }

    fun purgeAll() {
        runEntityService.purgeAll()
    }

    fun createScanInfo(run: Run): ScanInfo {
        val site = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodes = run.nodeScanById.filterValues { it.status != NodeScanStatus.UNDISCOVERED_0}.size
        val nodesText = if (run.scanDuration != null) "${nodes}" else "${nodes}+"
        val userName = userEntityService.getById(run.initiatorId).name
        val efficiencyText = efficiencyText(run)
        return ScanInfo(run.runId, site.name, site.siteId, userName, nodesText, efficiencyText)
    }

    private fun efficiencyText(run: Run): String {
        if (run.scanStartTime == null) {
            return "(not started)"
        }
        if (run.scanEfficiency == null) {
            return "(scanning)"
        }
        return "${run.scanEfficiency}%"
    }

    fun updateScanInfoToPlayers(run: Run) {
        val scanInfo = createScanInfo(run)
        runEntityService.getUsersOfScan(run.runId).forEach{ userId ->
            stompService.toUser(userId, ReduxActions.SERVER_UPDATE_SCAN_INFO, scanInfo)
        }
    }
}