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
    fun siteFull(siteId: String, principal: Principal) {
        executor.run(principal) { editorService.sendSiteFull(siteId) }
    }

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
        executor.run(principal) { editorService.update(command, principal) }
    }

    @MessageMapping("/deleteConnections")
    fun deleteConnections(command: DeleteNodeCommand, principal: Principal) {
        executor.run(principal) { editorService.deleteConnections(command) }
    }

    @MessageMapping("/deleteNode")
    fun deleteNode(command: DeleteNodeCommand, principal: Principal) {
        executor.run(principal) { editorService.deleteNode(command) }
    }

    @MessageMapping("/snap")
    fun snap(command: SiteCommand, principal: Principal) {
        executor.run(principal) { editorService.snap(command) }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    @MessageExceptionHandler
    @SendToUser("/error")
    fun handleException(exception: Exception): NotyMessage {
        if (exception is ValidationException) {
            return exception.getNoty()
        }
        logger.error(exception.message, exception)
        return NotyMessage("fatal", "Server error", exception.message ?: "")
    }
}