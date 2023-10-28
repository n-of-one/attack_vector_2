package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.layer.other.CoreLayer
import org.n1.av2.backend.entity.site.layer.other.TextLayer
import org.n1.av2.backend.service.run.terminal.scanning.ScanningService
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class CoreLayerService(
    private val stompService: StompService,
    private val scanningService: ScanningService,
    private val runEntityService: RunEntityService,
) {

    fun hack(layer: CoreLayer, runId: String) {
        var hackingDetails = ""
        if (layer.revealNetwork) {
            val run = runEntityService.getByRunId(runId)
            scanningService.quickScan(run)
            hackingDetails += "revealed network."
        }
        else {
            hackingDetails += "nothing found."
        }

        stompService.replyTerminalReceive("Hacked: [pri]${layer.level}[/] ${layer.name}, $hackingDetails")
    }

    fun connect(layer: TextLayer, node: Node) {
        stompService.replyTerminalReceive("Access to UI denied")
    }
}
