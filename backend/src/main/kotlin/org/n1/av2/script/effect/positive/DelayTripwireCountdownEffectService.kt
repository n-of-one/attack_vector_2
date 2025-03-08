package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.effect.helper.ScriptEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.DELAY_TRIPWIRE_COUNTDOWN
 */
@Service
class DelayTripwireCountdownEffectService(
    private val nodeEntityService: NodeEntityService,
    private val timerService: TimerService,
    private val timerEntityService: TimerEntityService,
    private val scriptEffectHelper: ScriptEffectHelper,

    ) : ScriptEffectInterface {

    override val name = "Increase running tripwire timer"
    override val defaultValue = "00:01:00"

    override val gmDescription = "When running this script in a node with a tripwire that has an active countdown timer, " +
        "this timer will increase to give the players more time."

    override fun playerDescription(effect: ScriptEffect) = "Delay a running tripwire countdown timer by" +
        " ${toHumanTime(effect.value!!)}."

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateDuration(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution {
        scriptEffectHelper.checkInNode(hackerState)?.let { return ScriptExecution(it) }

        val node = nodeEntityService.getById(hackerState.currentNodeId!!)
        val tripwireLayers = node.layers.filterIsInstance<TripwireLayer>()
        if (tripwireLayers.isEmpty()) {
            return ScriptExecution("This node has no tripwires.")
        }

        val timers = tripwireLayers.mapNotNull { layer -> timerEntityService.findByLayer(layer.id) }
        if (timers.isEmpty()) {
            return ScriptExecution("No tripwires in this node have active countdown timers.")
        }

        return ScriptExecution {
            tripwireLayers.forEach { layer ->
                timerService.delayTripwireTimer(layer, effect.value!!.toDuration(), hackerState.siteId!!)
            }
            TerminalLockState.UNLOCK
        }
    }
}
