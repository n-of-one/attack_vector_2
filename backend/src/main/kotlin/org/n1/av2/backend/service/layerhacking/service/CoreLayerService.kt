package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.engine.TaskEngine
import org.n1.av2.backend.engine.TaskIdentifiers
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.service.Timer
import org.n1.av2.backend.entity.service.TimerEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.CoreLayer
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.entity.site.layer.other.TripwireLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.terminal.scanning.InitiateScanService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CoreLayerService(
    private val stompService: StompService,
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

        stompService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name},$hackingDetails")
    }

    private fun stopTimers(runId: String, layer: CoreLayer): List<Timer> {
        val run = runEntityService.getByRunId(runId)
        val tripwires = nodeEntityService.getAll(run.siteId)
            .flatMap { node ->
                node.layers
                    .filterIsInstance<TripwireLayer>()
                    .filter { tripwireLayer -> tripwireLayer.coreLayerId == layer.id }
            }
        val timers = tripwires
            .map { tripwireLayer -> timerEntityService.findByLayer(tripwireLayer.id) }
            .filterNotNull()
        timers.forEach { timer ->
            timerEntityService.deleteById(timer.id)

            val identifiers = TaskIdentifiers(null, null, timer.layerId)
//            timedTaskRunner.removeAll(identifiers)
            taskEngine.removeAll(identifiers)


            stompService.toSite(run.siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timer.id)
        }
        return timers
    }

    fun connect(layer: TextLayer, node: Node) {
        stompService.replyTerminalReceive("Access to UI denied")
    }
}
