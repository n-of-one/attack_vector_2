package org.n1.av2.run.terminal.inside.skillbased

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.Skill
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.terminal.MISSING_SKILL_RESPONSE
import org.n1.av2.run.terminal.inside.InsideTerminalHelper
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.stereotype.Service

@Service
class CommandWeakenService(
    private val connectionService: ConnectionService,
    private val iceService: IceService,
    private val insideTerminalHelper: InsideTerminalHelper,
    private val nodeEntityService: NodeEntityService,
    private val skillService: SkillService,
) {

    fun processWeaken(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!insideTerminalHelper.verifyInside(hackerState)) return
        requireNotNull(hackerState.currentNodeId)
        val skills = skillService.findSkillsForUser(hackerState.userId)
        val weakenSkills = skills.filter { it.type == SkillType.WEAKEN }

        if (weakenSkills.isEmpty()) {
            connectionService.replyTerminalReceive(MISSING_SKILL_RESPONSE)
            return
        }

        if (arguments.isEmpty()) {
            processWeakenStatus(weakenSkills, hackerState)
            return
        }

        val (layer, skill) = checkPrerequisites(arguments, hackerState, skills) ?: return

        weaken(layer, hackerState, skill)
    }

    fun processWeakenStatus(weakenSkills: List<Skill>, hackerState: HackerStateRunning) {
        connectionService.replyTerminalReceive("Overview of uses of weaken:")
        weakenSkills.forEach { skill ->
            val usedOnThisSite = skill.usedOnSiteIds.contains(hackerState.siteId)
            val siteText = if (usedOnThisSite) "[warn]already used on this site" else "[ok]can be used on this site"
            connectionService.replyTerminalReceive("- ${skill.value} - $siteText")
        }
    }


    fun checkPrerequisites(
        arguments: List<String>,
        hackerState: HackerStateRunning,
        weakenSkills: List<Skill>
    ): Pair<IceLayer, Skill>? {
        val layer = insideTerminalHelper.verifyCanAccessLayer(arguments.first(), hackerState, "weaken") ?: return null
        if (layer !is IceLayer) {
            connectionService.replyTerminalReceive("Layer [primary]${layer.level}[/] is not an ICE layer.")
            return null
        }
        if (layer.strength == IceStrength.VERY_WEAK) {
            connectionService.replyTerminalReceive("Cannot decrease the strength of this ICE layer, strength is already [info]Very weak")
            return null
        }

        val skill = checkSkill(weakenSkills, hackerState, layer) ?: return null

        return layer to skill
    }

    private fun checkSkill(weakenSkills: List<Skill>, hackerState: HackerStateRunning, layer: IceLayer): Skill? {

        val weakenSkillsThatWorkOnThisIce = weakenSkills.filter {
            it.value?.uppercase()?.contains(layer.type.toString().substringBefore("_ICE")) == true
        }

        if (weakenSkillsThatWorkOnThisIce.isEmpty()) {
            connectionService.replyTerminalReceive("Weaken not compatible with ICE type. [mute](Skill does not support this)")
            return null
        }

        val availableWeakenSkills =
            weakenSkillsThatWorkOnThisIce.filter { skill -> !skill.usedOnSiteIds.contains(hackerState.siteId) }
        if (availableWeakenSkills.isEmpty()) {
            connectionService.replyTerminalReceive("Tamper attempt blocked. [mute](Skill already used on this site)")
            return null
        }

        val sortedSkills =
            availableWeakenSkills.sortedBy { it.value?.split(",")?.size } // skills that support the least types of ice used up first
        return sortedSkills.first()
    }

    private fun weaken(layer: IceLayer, hackerState: HackerStateRunning, skill: Skill) {
        val newStrength = IceStrength.Companion.forValue(layer.strength.value - 1)
        val newIceLayer = iceService.createIceLayer(layer, layer.type, newStrength, layer.name)

        val node = nodeEntityService.findByLayerId(layer.id)
        iceService.changeIce(node, layer, newIceLayer)
        skillService.useSkillOnSite(skill.id, hackerState.siteId)

        connectionService.replyTerminalReceive("ICE weakened. New strength: [info]${newStrength.description}")
    }
}
