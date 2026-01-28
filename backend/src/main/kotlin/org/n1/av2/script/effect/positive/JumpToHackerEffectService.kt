package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateRepo
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.terminal.generic.SocialTerminalService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.JumpBlockedType
import org.n1.av2.script.effect.helper.JumpEffectHelper
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service
/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.JUMP_TO_HACKER
 */
@Service
class JumpToHackerEffectService(
    private val scriptEffectHelper: ScriptEffectHelper,
    private val hackerStateRepo: HackerStateRepo,
    private val userEntityService: UserEntityService,
    private val jumpEffectHelper: JumpEffectHelper,
    ) : ScriptEffectInterface {

    override val name = "Jump to hacker"
    override val defaultValue = JumpBlockedType.BLOCKED_BY_ICE.name
    override val gmDescription = "Jump to another hacker"

    override fun playerDescription(effect: ScriptEffect): String{
        val suffix = if (effect.value == JumpBlockedType.BLOCKED_BY_ICE.name) ", blocked by ICE along the way." else ", not blocked by ICE."
        return gmDescription + suffix
    }

    override fun validate(effect: ScriptEffect) = JumpEffectHelper.validateJumpBlockedType(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }
        val userName = argumentTokens.firstOrNull() ?: return ScriptExecution("Provide the [info]<username>[/] of the hacker to phase to.")

        val user = userEntityService.findByNameIgnoreCase(userName)
        if (user == null) {
            return ScriptExecution(SocialTerminalService.hackerNotFound(userName))
        }

        val targetHackerRunState: HackerState = hackerStateRepo.findById(user.id).orElse(null) ?: return ScriptExecution("${userName} is not in this run.")
        if (targetHackerRunState.runId != hackerState.runId) return ScriptExecution("${userName} is not in this run.")
        if (targetHackerRunState.activity != HackerActivity.INSIDE) return ScriptExecution("${userName} is not in the site.")

        val currentNodeId = hackerState.currentNodeId ?: error("Current node ID not found.")
        val targetNodeId = targetHackerRunState.currentNodeId ?: error("Target node ID not found.")

        return jumpEffectHelper.jump(effect, hackerState.siteId, currentNodeId, targetNodeId, hackerState, userName)

    }
}
