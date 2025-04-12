package org.n1.av2.run.terminal.outside

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.InitiateScanService
import org.n1.av2.run.terminal.MISSING_SKILL_RESPONSE
import org.n1.av2.run.terminal.generic.DevCommandHelper
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

@Service
class CommandScanService(
    private val connectionService: ConnectionService,
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
    private val initiateScanService: InitiateScanService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val currentUser: CurrentUserService,
    private val devCommandHelper: DevCommandHelper,
    private val hackerEntityService: HackerEntityService,
    private val outsideTerminalHelper: OutsideTerminalHelper,
) {

    fun processScan(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!checkHasScanSkill()) return

        if (hackerState.activity == HackerActivity.OUTSIDE) processScanFromOutside(arguments, hackerState)
        if (hackerState.activity == HackerActivity.INSIDE) processScanFromInside(arguments, hackerState)
    }

    private fun processScanFromOutside(arguments: List<String>, hackerState: HackerStateRunning) {
        if (arguments.isNotEmpty()) {
            connectionService.replyTerminalReceive("Ignoring arguments, scanning from outside")
        }
        if (!checkHasScanSkill()) return
        if (!outsideTerminalHelper.verifySiteNotShutdown(hackerState.siteId)) return

        val run = runEntityService.getByRunId(hackerState.runId)
        val networkId = sitePropertiesEntityService.getBySiteId(run.siteId).startNodeNetworkId

        val node = nodeEntityService.findByNetworkId(run.siteId, networkId)
            ?: return connectionService.replyTerminalReceive("[error]Network error[/] no route to this site.")

        connectionService.replyTerminalSetLocked(true)
        initiateScanService.scanFromOutside(run, node)
    }

    private fun processScanFromInside(arguments: List<String>, hackerState: HackerStateRunning) {
        if (arguments.isNotEmpty()) {
            connectionService.replyTerminalReceive("Ignoring arguments, scanning current node")
        }
        connectionService.replyTerminalSetLocked(true)
        requireNotNull(hackerState.currentNodeId)
        val run = runEntityService.getByRunId(hackerState.runId)
        val node = nodeEntityService.findById(hackerState.currentNodeId)
        initiateScanService.scanFromInside(run, node)
    }

    private fun checkHasScanSkill(): Boolean {
        val hacker = hackerEntityService.findForUser(currentUser.userEntity)
        val hasScanSkill = hacker.hasSkill(SkillType.SCAN)

        if (!hasScanSkill) {
            connectionService.replyTerminalReceive(MISSING_SKILL_RESPONSE)
            return false
        }
        return true
    }

    fun processQuickScan(hackerState: HackerStateRunning) {
        if (!devCommandHelper.checkDevModeEnabled()) return


        if (!outsideTerminalHelper.verifyOutside(hackerState)) return
        val run = runEntityService.getByRunId(hackerState.runId)

        initiateScanService.quickScan(run)
        connectionService.replyTerminalReceive("QuickScanned.")
    }

}
