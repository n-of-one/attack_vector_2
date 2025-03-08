package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.JumpBlockedType
import org.n1.av2.script.effect.helper.JumpEffectHelper
import org.n1.av2.script.effect.helper.NodeAccessHelper
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.JUMP_TO_NODE
 */
@Service
class JumpToNodeEffectService(
    private val nodeEntityService: NodeEntityService,
    private val scriptEffectHelper: ScriptEffectHelper,
    private val nodeAccessHelper: NodeAccessHelper,
    private val jumpEffectHelper: JumpEffectHelper,
) : ScriptEffectInterface {

    override val name = "Jump to node"
    override val defaultValue = JumpBlockedType.BLOCKED_BY_ICE.name
    override val gmDescription = "Jump to another node"

    override fun playerDescription(effect: ScriptEffect): String{
        val suffix = if (effect.value == JumpBlockedType.BLOCKED_BY_ICE.name) ", blocked by ICE along the way." else ", not blocked by ICE."
        return gmDescription + suffix
    }

    override fun validate(effect: ScriptEffect) = null

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val targetNetworkId = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [ok]network id[/] of the node to jump to.")
        val targetNode = nodeEntityService.findByNetworkId(hackerState.siteId!!, targetNetworkId)
        nodeAccessHelper.checkNodeRevealed(targetNode, targetNetworkId, hackerState.runId!!)?.let { return ScriptExecution(it) }

        val currentNodeId = hackerState.currentNodeId ?: error("Current node ID not set.")

        return jumpEffectHelper.jump(effect, hackerState.siteId, currentNodeId, targetNode!!.id, hackerState, targetNetworkId)
    }
}
