package org.n1.av2.backend.web.ws

import mu.KLogging
import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.run.HackingService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class HackingController(
        val executor: SerializingExecutor,
        val hackingService: HackingService
) {

    companion object: KLogging()

    data class NodeBody(val nodeId: String, val runId: String)

    @MessageMapping("/hack/moveArrive")
    fun scansOfPlayer(command: NodeBody, principal: Principal) {
        executor.run(principal) { hackingService.moveArrive(command.nodeId, command.runId) }
    }

    @MessageMapping("/hack/probedLayers")
    fun probedServices(command: NodeBody, principal: Principal) {
        executor.run(principal) { hackingService.probedLayers(command.nodeId, command.runId) }
    }

    @MessageMapping("/hack/probedConnections")
    fun probedConnections(command: NodeBody, principal: Principal) {
        executor.run(principal) { hackingService.probedConnections(command.nodeId, command.runId) }
    }


    data class LeashArriveHacker(val nodeId: String, val runId: String)

    @MessageMapping("/hack/leashArriveHacker")
    fun leashArriveHacker(command: LeashArriveHacker, principal: Principal) {
        executor.run(principal) { hackingService.leashArriveHacker(command.nodeId, command.runId) }
    }

}