package org.n1.mainframe.backend.web.ws

import mu.KLogging
import org.n1.mainframe.backend.engine.SerializingExecutor
import org.n1.mainframe.backend.model.ui.*
import org.n1.mainframe.backend.service.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.handler.annotation.MessageExceptionHandler


@Controller
class EditorController(
        val editorService: EditorService,
        val executor: SerializingExecutor
) {
    companion object : KLogging()

    @MessageMapping("/siteFull")
    fun siteFull(siteId: String) {
        executor.run{ editorService.sendSiteFull(siteId) }
    }

    @MessageMapping("/addNode")
    fun addNode(command: AddNode) {
        executor.run{ editorService.addNode(command) }
    }

    @MessageMapping("/moveNode")
    fun moveNode(command: MoveNode) {
        executor.run{ editorService.moveNode(command) }
    }

    @MessageMapping("/addConnection")
    fun addConnection(command: AddConnection) {
        executor.run{ editorService.addConnection(command) }
    }

    @MessageMapping("/editSiteData")
    fun editSiteData(command: EditSiteData, principal: Principal) {
        executor.run{ editorService.update(command, principal) }
    }

    @MessageMapping("/deleteConnections")
    fun deleteConnections(command: DeleteNodeCommand) {
        executor.run{ editorService.deleteConnections(command) }
    }

    @MessageMapping("/deleteNode")
    fun deleteNode(command: DeleteNodeCommand) {
        executor.run{ editorService.deleteNode(command) }
    }

    @MessageMapping("/snap")
    fun snap(command: SiteCommand) {
        executor.run{ editorService.snap(command) }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/error")
    fun handleException(exception: Exception): NotyMessage {
        if (exception is ValidationException) {
            return exception.getNoty()
        }
        logger.error(exception.message, exception)
        return NotyMessage("fatal", "Server error", exception.message ?: "" )
    }
}