package org.n1.av2.layer.other.core

import org.n1.av2.timer.Timer
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.TaskEngine
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.InitiateScanService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class CoreLayerService(
    private val connectionService: ConnectionService,
    private val initiateScanService: InitiateScanService,
    private val runEntityService: RunEntityService,
    private val nodeEntityService: NodeEntityService,
    private val timerEntityService: TimerEntityService,
    private val taskEngine: TaskEngine,
) {

    fun hack(layer: CoreLayer, runId: String) {
        var hackingDetails = ""
        if (layer.revealNetwork) {
            val run = runEntityService.getByRunId(runId)
            initiateScanService.quickScan(run)
            hackingDetails += " revealed network"
        }

        val timers = stopTimers(runId, layer)
        if (timers.isNotEmpty()) {
            hackingDetails += " stopped timer"
        }

        if (hackingDetails.isEmpty()) {
            hackingDetails = " no effect"
        }

        connectionService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name},$hackingDetails")
    }

    private fun stopTimers(runId: String, layer: CoreLayer): List<Timer> {
        val run = runEntityService.getByRunId(runId)
        val tripwires = nodeEntityService.findBySiteId(run.siteId)
            .flatMap { node ->
                node.layers
                    .filterIsInstance<TripwireLayer>()
                    .filter { tripwireLayer -> tripwireLayer.coreLayerId == layer.id }
            }

        val timers:List<Timer> = tripwires.mapNotNull { tripwireLayer ->
            val timer = timerEntityService.findByLayer(tripwireLayer.id)
            if ( timer == null) {
                null
            }
            else {
                timerEntityService.deleteById(timer.id)

                val identifiers = mapOf("layerId" to tripwireLayer.id)
                taskEngine.removeAll(identifiers)

                connectionService.toSite(run.siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timer.id)
                timer
            }
        }

        return timers
    }

}
