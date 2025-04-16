package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.hacker.skill.SkillUseService
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.terminal.MISSING_SKILL_RESPONSE
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.stereotype.Service

@Service
class CommandWeakenService(
    private val connectionService: ConnectionService,
    private val skillUseService: SkillUseService,
    private val iceService: IceService,
    private val insideTerminalHelper: InsideTerminalHelper,
    private val nodeEntityService: NodeEntityService,
    private val hackerEntityService:  HackerEntityService,
) {

    fun processWeaken(arguments: List<String>, hackerState: HackerStateRunning) {
        val hacker = hackerEntityService.findForUserId(hackerState.userId)

        val layer = checkPrerequisites(arguments, hackerState, hacker) ?: return
        if (!canUseSkillAgain(hackerState, hacker)) return

        weaken(layer, hackerState, hacker)
    }

    fun checkPrerequisites(arguments: List<String>, hackerState: HackerStateRunning, hacker: Hacker) : IceLayer?{
        if (!insideTerminalHelper.verifyInside(hackerState)) return null
        requireNotNull(hackerState.currentNodeId)

        if (!hacker.hasSkill(SkillType.WEAKEN)) {
            connectionService.replyTerminalReceive(MISSING_SKILL_RESPONSE)
            return null
        }

        if (arguments.isEmpty()) {
            connectionService.replyTerminalReceive("Missing [primary]<layer>[/]      -- for example: [b]weaken [primary] 1")
            return null
        }
        val layer = insideTerminalHelper.verifyCanAccessLayer(arguments.first(), hackerState, "weaken") ?: return null
        if (layer !is IceLayer) {
            connectionService.replyTerminalReceive("Layer [primary]${layer.level}[/] is not an ICE layer.")
            return null
        }
        if (layer.strength == IceStrength.VERY_WEAK) {
            connectionService.replyTerminalReceive("Cannot decrease the strength of this ICE layer, strength is already [info]Very weak")
            return null
        }

        if (!hacker.skillContainingValue(SkillType.WEAKEN, layer.type.toString().substringBefore("_ICE"))) {
            connectionService.replyTerminalReceive("Weaken not compatible with ICE type. [mute](Skill does not support this)")
            return null
        }

        return layer
    }

    private fun canUseSkillAgain(hackerState: HackerStateRunning, hacker: Hacker): Boolean {
        if (!skillUseService.canUseSkillOnSite(hacker, SkillType.WEAKEN, hackerState.siteId)) {
            connectionService.replyTerminalReceive("Tamper attempt blocked. [mute](Skill already used on this site)")
            return false
        }
        return true
    }

    private fun weaken(layer: IceLayer, hackerState: HackerStateRunning, hacker: Hacker) {
        val newStrength = IceStrength.forValue(layer.strength.value - 1)
        val newIceLayer = iceService.createIceLayer(layer, layer.type, newStrength, layer.name)

        val node = nodeEntityService.findByLayerId(layer.id)
        iceService.changeIce(node, layer, newIceLayer)

        connectionService.replyTerminalReceive("ICE weakened. New strength: [info]${newStrength.description}")

        skillUseService.skillUsed(hacker, SkillType.WEAKEN, hackerState.siteId)
    }
}
