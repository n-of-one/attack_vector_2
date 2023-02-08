package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.TaskRunner
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
        val taskRunner: TaskRunner
) {
    private val logger = mu.KotlinLogging.logger {}

    @MessageMapping("/editor/siteFull")
    fun siteFull(siteId: String, principal: Principal) {
        taskRunner.runTask(principal) { editorService.sendSiteFull(siteId) }
    }

    @RolesAllowed()
    @MessageMapping("/editor/addNode")
    fun addNode(command: AddNode, principal: Principal) {
        taskRunner.runTask(principal) { editorService.addNode(command) }
    }

    @MessageMapping("/editor/moveNode")
    fun moveNode(command: MoveNode, principal: Principal) {
        taskRunner.runTask(principal) { editorService.moveNode(command) }
    }

    @MessageMapping("/editor/addConnection")
    fun addConnection(command: AddConnection, principal: Principal) {
        taskRunner.runTask(principal) { editorService.addConnection(command) }
    }

    @MessageMapping("/editor/editSiteData")
    fun editSiteData(command: EditSiteData, principal: Principal) {
        taskRunner.runTask(principal) { editorService.updateSiteData(command) }
    }

    data class DeleteCommand(val siteId: String = "", val nodeId: String = "" )
    @MessageMapping("/editor/deleteConnections")
    fun deleteConnections(command: DeleteCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.deleteConnections(command.siteId, command.nodeId) }
    }

    @MessageMapping("/editor/deleteNode")
    fun deleteNode(command: DeleteCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.deleteNode(command.siteId, command.nodeId) }
    }

    data class SnapCommand(val siteId: String = "")
    @MessageMapping("/editor/snap")
    fun snap(command: SnapCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.snap(command.siteId) }
    }

    @MessageMapping("/editor/editNetworkId")
    fun editNetworkId(command: EditNetworkIdCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.editNetworkId(command) }
    }

    @MessageMapping("/editor/editLayerData")
    fun editServiceData(command: EditLayerDataCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.editLayerData(command) }
    }

    @MessageMapping("/editor/addLayer")
    fun addService(command: AddLayerCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.addLayer(command) }
    }

    @MessageMapping("/editor/removeLayer")
    fun removeService(command: RemoveLayerCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.removeLayer(command) }
    }

    @MessageMapping("/editor/swapLayers")
    fun swapServiceLayer(command: SwapLayerCommand, principal: Principal) {
        taskRunner.runTask(principal) { editorService.swapLayers(command) }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}