package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.editor.EditorService
import org.n1.av2.backend.util.toServerFatalReduxEvent
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.stereotype.Controller
import javax.annotation.security.RolesAllowed

@Controller
class EditorController(
    val editorService: EditorService,
    val taskRunner: TaskRunner
) {
    private val logger = mu.KotlinLogging.logger {}

    @MessageMapping("/editor/siteFull")
    fun siteFull(siteId: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.sendSiteFull(siteId) }
    }

    @RolesAllowed()
    @MessageMapping("/editor/addNode")
    fun addNode(command: AddNode, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.addNode(command) }
    }

    @MessageMapping("/editor/moveNode")
    fun moveNode(command: MoveNode, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.moveNode(command) }
    }

    @MessageMapping("/editor/addConnection")
    fun addConnection(command: AddConnection, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.addConnection(command) }
    }

    @MessageMapping("/editor/editSiteProperty")
    fun editSiteData(command: EditSiteProperty, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.updateSiteProperties(command) }
    }

    data class DeleteCommand(val siteId: String = "", val nodeId: String = "" )
    @MessageMapping("/editor/deleteConnections")
    fun deleteConnections(command: DeleteCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.deleteConnections(command.siteId, command.nodeId) }
    }

    @MessageMapping("/editor/deleteNode")
    fun deleteNode(command: DeleteCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.deleteNode(command.siteId, command.nodeId) }
    }

    data class SnapCommand(val siteId: String = "")
    @MessageMapping("/editor/snap")
    fun snap(command: SnapCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.snap(command.siteId) }
    }

    @MessageMapping("/editor/editNetworkId")
    fun editNetworkId(command: EditNetworkIdCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.editNetworkId(command) }
    }

    @MessageMapping("/editor/editLayerData")
    fun editServiceData(command: EditLayerDataCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.editLayerData(command) }
    }

    @MessageMapping("/editor/addLayer")
    fun addLayer(command: AddLayerCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.addLayer(command) }
    }

    @MessageMapping("/editor/removeLayer")
    fun removeService(command: RemoveLayerCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.removeLayer(command) }
    }

    @MessageMapping("/editor/swapLayers")
    fun swapServiceLayer(command: SwapLayerCommand, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { editorService.swapLayers(command) }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}