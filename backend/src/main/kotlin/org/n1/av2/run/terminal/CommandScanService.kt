package org.n1.av2.run.terminal

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.InitiateScanService
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
    private val configService: ConfigService,
    private val hackerEntityService: HackerEntityService,
) {

    fun processScanFromOutside(run: Run) {
        if (!checkHasScanSkill()) return

        val networkId = sitePropertiesEntityService.getBySiteId(run.siteId).startNodeNetworkId

        val node = nodeEntityService.findByNetworkId(run.siteId, networkId)
            ?: return connectionService.replyTerminalReceive("[error]Network error[/] no route to this site.")

        connectionService.replyTerminalSetLocked(true)
        initiateScanService.scanFromOutside(run, node)
    }

    fun processScanFromInside(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (!checkHasScanSkill()) return

        if (tokens.size > 1) {
            connectionService.replyTerminalReceive("Ignoring arguments, scanning current node")
        }
        connectionService.replyTerminalSetLocked(true)
        val run = runEntityService.getByRunId(runId)
        val node = nodeEntityService.findById(state.currentNodeId)
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

    fun processQuickScan(run: Run) {
        if (!configService.getAsBoolean(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive("QuickScan is disabled.")
            return
        }

        initiateScanService.quickScan(run)
        connectionService.replyTerminalReceive("QuickScanned.")
    }
}
