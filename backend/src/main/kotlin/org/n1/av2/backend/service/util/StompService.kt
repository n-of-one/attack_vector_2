package org.n1.av2.backend.service.util

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ReduxEvent
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.run.TERMINAL_MAIN
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StompService(
    val stompTemplate: SimpMessageSendingOperations,
    val currentUserService: CurrentUserService,
    val config: ServerConfig

) {

    private val logger = mu.KotlinLogging.logger {}

    @PostConstruct
    fun logEnvironment() {
        logger.info("ENVIRONMENT: ${config.environment}")
        if (config.dev) {
            logger.info("Simulating non-localhost")
        }
    }

    val simulateNonLocalhost = {
        if (config.dev) {
            Thread.sleep(70)
        }
    }

    private fun sendToDestination(path: String, actionType: ServerActions, data: Array<*>) {
        simulateNonLocalhost()
        logger.debug("-> ${path} ${actionType}")

        val processedData = processData(data)
        val event = ReduxEvent(actionType, processedData)
        stompTemplate.convertAndSend(path, event)
    }

    private fun processData(data: Array<*>): Any? {
        if (data.isEmpty()) {
            return null
        }
        val dataAsPairs = data.filterIsInstance<Pair<*, *>>()
        if (dataAsPairs.size == data.size) {
            val map = HashMap<String, Any?>()
            dataAsPairs.forEach { pair  ->
                val first = pair.first ?: error("null keys not supported for stomp call")
                if (first !is String) error("keys must be strings, otherwise it's invalid json")
                map[first] = pair.second
            }
            return map
        }
        if (data.size == 1) {
            return data[0]
        }
        error("Cannot send multi-arg if it's not all pairs")
    }

    fun toSite(siteId: String, actionType: ServerActions, vararg data: Any) {
        sendToDestination("/topic/site/${siteId}", actionType, data)
    }

    fun toRun(runId: String, actionType: ServerActions, vararg data: Any) {
        sendToDestination("/topic/run/${runId}", actionType, data)
    }

    fun toIce(iceId: String, actionType: ServerActions, vararg data: Any) {
        sendToDestination("/topic/ice/${iceId}", actionType, data)
    }

    fun toApp(appId: String, actionType: ServerActions, vararg data: Any) {
        sendToDestination("/topic/app/${appId}", actionType, data)
    }

    fun toUser(userId: String, actionType: ServerActions, vararg data: Any) {
        sendToDestination("/topic/user/${userId}", actionType, data)
    }

    fun reply(actionType: ServerActions, vararg data: Any) {
        if (currentUserService.isSystemUser) error("Cannot reply to system user")
        val name = SecurityContextHolder.getContext().authentication.name
        sendToDestination("/topic/user/${name}", actionType, data)
    }

    //**********************************************************************************************************************//

    fun replyMessage(message: NotyMessage) {
        reply(ServerActions.SERVER_NOTIFICATION, message)
    }

    fun replyNeutral(message: String, title: String = "") {
        reply(ServerActions.SERVER_NOTIFICATION, NotyMessage(NotyType.NEUTRAL, title, message))
    }

    fun replyError(message: String, title: String = "") {
        reply(ServerActions.SERVER_NOTIFICATION, NotyMessage(NotyType.ERROR, title, message))
    }

    class TerminalReceive(val terminalId: String, val lines: Array<out String>, val locked: Boolean? = null)

    fun replyTerminalReceiveAndLocked(locked: Boolean, vararg lines: String) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines, locked))
    }

    fun replyTerminalReceive(vararg lines: String) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines))
    }

    fun replyTerminalSetLocked(lock: Boolean) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, emptyArray(), lock))
    }


}