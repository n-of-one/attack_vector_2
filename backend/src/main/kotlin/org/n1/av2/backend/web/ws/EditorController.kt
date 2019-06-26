package org.n1.av2.backend.web.ws

import mu.KLogging
import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.EditorService
import org.n1.av2.backend.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import java.security.Principal
import javax.annotation.security.RolesAllowed


@Controller
class EditorController(
        val editorService: EditorService,
        val executor: SerializingExecutor
) {
    companion object : KLogging()

    @MessageMapping("/editor/siteFull")
    fun siteFull(siteId: String, principal: Principal) {
        executor.run(principal) { editorService.sendSiteFull(siteId) }
    }

    @RolesAllowed()
    @MessageMapping("/editor/addNode")
    fun addNode(command: AddNode, principal: Principal) {
        executor.run(principal) { editorService.addNode(command) }
    }

    @MessageMapping("/editor/moveNode")
    fun moveNode(command: MoveNode, principal: Principal) {
        executor.run(principal) { editorService.moveNode(command) }
    }

    @MessageMapping("/editor/addConnection")
    fun addConnection(command: AddConnection, principal: Principal) {
        executor.run(principal) { editorService.addConnection(command) }
    }

    @MessageMapping("/editor/editSiteData")
    fun editSiteData(command: EditSiteData, principal: Principal) {
        executor.run(principal) { editorService.updateSiteData(command) }
    }

    data class DeleteCommand(val siteId: String = "", val nodeId: String = "" )
    @MessageMapping("/editor/deleteConnections")
    fun deleteConnections(command: DeleteCommand, principal: Principal) {
        executor.run(principal) { editorService.deleteConnections(command.siteId, command.nodeId) }
    }

    @MessageMapping("/editor/deleteNode")
    fun deleteNode(command: DeleteCommand, principal: Principal) {
        executor.run(principal) { editorService.deleteNode(command.siteId, command.nodeId) }
    }

    data class SnapCommand(val siteId: String = "")
    @MessageMapping("/editor/snap")
    fun snap(command: SnapCommand, principal: Principal) {
        executor.run(principal) { editorService.snap(command.siteId) }
    }

    @MessageMapping("/editor/editNetworkId")
    fun editNetworkId(command: EditNetworkIdCommand, principal: Principal) {
        executor.run(principal) { editorService.editNetworkId(command) }
    }

    @MessageMapping("/editor/editServiceData")
    fun editServiceData(command: EditServiceDataCommand, principal: Principal) {
        executor.run(principal) { editorService.editServiceData(command) }
    }

    @MessageMapping("/editor/addService")
    fun addService(command: AddServiceCommand, principal: Principal) {
        executor.run(principal) { editorService.addService(command) }
    }

    @MessageMapping("/editor/removeService")
    fun removeService(command: RemoveServiceCommand, principal: Principal) {
        executor.run(principal) { editorService.removeService(command) }
    }

    @MessageMapping("/editor/swapServiceLayer")
    fun swapServiceLayer(command: SwapServiceCommand, principal: Principal) {
        executor.run(principal) { editorService.swapServiceLayer(command) }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}