package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service


@Service
class DelayTripwireCountdownService(
    private val nodeEntityService: NodeEntityService,
    private val timerService: TimerService,

    private val timerEntityService: TimerEntityService,

    ): ScriptEffectInterface {

    override val name = "Increase running tripwire timer"
    override val defaultValue = "00:01:00"

    override val gmDescription = "When running this script in a node with a tripwire that has an active countdown timer, " +
        "this timer will increase to give the players more time."

    override fun playerDescription(effect: ScriptEffect) = "Delay a running tripwire countdown timer by" +
        " ${toHumanTime(effect.value!!)}."

    override fun validate(effect: ScriptEffect): String? {
        if (effect.value == null) return "Duration is required."
        return effect.value.validateDuration()
    }

    override fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerState: HackerState): String? {
        if (hackerState.activity == HackerActivity.OUTSIDE || hackerState.currentNodeId == null) {
            return "You can only run this script inside a node."
        }

        val node = nodeEntityService.getById(hackerState.currentNodeId)
        val tripwireLayers = node.layers.filterIsInstance<TripwireLayer>()
        if (tripwireLayers.isEmpty()) {
            return "This node has no tripwires."
        }

        val timers = tripwireLayers.mapNotNull { layer -> timerEntityService.findByLayer(layer.id) }
        if (timers.isEmpty()) {
            return "No tripwires in this node have active countdown timers."
        }

        return null
    }

    override fun execute(effect: ScriptEffect, strings: List<String>, hackerState: HackerState): TerminalLockState {
        val node = nodeEntityService.getById(hackerState.currentNodeId!!)
        val tripwireLayers = node.layers.filterIsInstance<TripwireLayer>()

        tripwireLayers.forEach { layer ->
            timerService.delayTripwireTimer(layer, effect.value!!.toDuration(), hackerState.siteId!!)
        }

        return TerminalLockState.UNLOCK
    }
}
