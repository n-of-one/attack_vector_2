package org.n1.av2.script.scripts

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.layer.other.tripwire.TripwireLayerService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.util.pluralS
import org.n1.av2.platform.util.toDuration
import org.n1.av2.script.Script
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class TripwireExtraTimeScript(
    private val tripwireLayerService: TripwireLayerService,
    private val connectionService: ConnectionService,
    private val nodeEntityService: NodeEntityService,
) {

    fun runScript(script: Script, hackerSate: HackerState): Boolean {

        // TODO: Implement this method
//        if (hackerSate.activity != HackerActivity.INSIDE) {
//            connectionService.replyTerminalReceive("Script execution failed: [error]883[/] You must be inside a node to run this script.")
//            return false
//        }
//
//        val currentNodeId = hackerSate.currentNodeId ?: error("Script execution failed: [error]879[/] currently not in a node.")
//        val node = nodeEntityService.findById(currentNodeId)
//
//        val tripWireLayers = node.layers.filterIsInstance<TripwireLayer>()
//        if (tripWireLayers.isEmpty()) {
//            connectionService.replyTerminalReceive("Script execution failed: [error]207[/] no tripwires found in this node.")
//            return false
//        }
//
//        val scriptDuration = script.value?.toDuration() ?: error("script misconfigured: no value")
//        val extendDuration = scriptDuration.plusSeconds(1) // add 1 second to compensate for the time it takes to execute the script.
//
//        val siteId = hackerSate.siteId ?: error("Script execution failed: [error]888[/] currently not in a site.")
//
//
//        val tripwiresUpdate = tripWireLayers
//            .map { layer ->
//                tripwireLayerService.increaseTimer(layer, extendDuration, siteId)
//            }
//            .filter{ it }
//            .size
//
//        if (tripwiresUpdate == 0) {
//            connectionService.replyTerminalReceive("Script execution failed: [error]209[/] there are no tripwires timers associated with layers in this node.")
//            return false
//        }
//
//        connectionService.replyTerminalReceive("Script execute [ok] success[/]. ${tripwiresUpdate} timer${pluralS(tripwiresUpdate)} extended by ${script.value}.")
//        return true
        return true

    }
}
