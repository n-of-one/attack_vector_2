package org.n1.mainframe.backend.web.ws

import mu.KLogging
import org.n1.mainframe.backend.engine.SerializingExecutor
import org.n1.mainframe.backend.model.ui.ReduxEvent
import org.n1.mainframe.backend.model.ui.site.command.AddConnection
import org.n1.mainframe.backend.model.ui.site.command.AddNode
import org.n1.mainframe.backend.model.ui.site.command.EditSiteData
import org.n1.mainframe.backend.model.ui.site.command.MoveNode
import org.n1.mainframe.backend.service.EditorService
import org.n1.mainframe.backend.util.toServerFatalReduxEvent
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

    @MessageMapping("/siteFull")
    fun siteFull(siteId: String, principal: Principal) {
        executor.run(principal) { editorService.sendSiteFull(siteId) }
    }

    @RolesAllowed()
    @MessageMapping("/addNode")
    fun addNode(command: AddNode, principal: Principal) {
        executor.run(principal) { editorService.addNode(command) }
    }

    @MessageMapping("/moveNode")
    fun moveNode(command: MoveNode, principal: Principal) {
        executor.run(principal) { editorService.moveNode(command) }
    }

    @MessageMapping("/addConnection")
    fun addConnection(command: AddConnection, principal: Principal) {
        executor.run(principal) { editorService.addConnection(command) }
    }

    @MessageMapping("/editSiteData")
    fun editSiteData(command: EditSiteData, principal: Principal) {
        executor.run(principal) { editorService.update(command) }
    }

    data class DeleteCommand(val siteId: String = "", val nodeId: String = "" )
    @MessageMapping("/deleteConnections")
    fun deleteConnections(command: DeleteCommand, principal: Principal) {
        executor.run(principal) { editorService.deleteConnections(command.siteId, command.nodeId) }
    }

    @MessageMapping("/deleteNode")
    fun deleteNode(command: DeleteCommand, principal: Principal) {
        executor.run(principal) { editorService.deleteNode(command.siteId, command.nodeId) }
    }

    data class SnapCommand(val siteId: String = "")
    @MessageMapping("/snap")
    fun snap(command: SnapCommand, principal: Principal) {
        executor.run(principal) { editorService.snap(command.siteId) }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/reply")
    fun handleException(exception: Exception): ReduxEvent {
        logger.error(exception.message, exception)
        return toServerFatalReduxEvent(exception)
    }
}