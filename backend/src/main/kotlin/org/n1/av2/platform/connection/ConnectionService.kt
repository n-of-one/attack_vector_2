package org.n1.av2.platform.connection

import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.frontend.model.ReduxEvent
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Configuration
class InitConnectionService(
    private val connectionService: ConnectionService,
    private val configService: ConfigService,
) {
    init {
        connectionService.configService = configService
    }
}

@Service
class ConnectionService(
    val stompTemplate: SimpMessageSendingOperations,
) {
    lateinit var configService: ConfigService

    private val logger = mu.KotlinLogging.logger {}

    val simulateNonLocalhost = {
        val delay = configService.getAsLong(ConfigItem.DEV_SIMULATE_NON_LOCALHOST_DELAY_MS)
        if (delay > 0) {
            Thread.sleep(delay)
        }
    }

    fun sendToDestination(path: String, actionType: ServerActions, data: Array<*>) {
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
        val user = (SecurityContextHolder.getContext().authentication?.principal as UserEntity?)
        if (user == null || user.type == UserType.SYSTEM || user.type == UserType.NOT_LOGGED_IN) return // don't reply if no user or system user

        val connection = SecurityContextHolder.getContext().authentication.name

        sendToDestination("/topic/user/${connection}", actionType, data)
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

    fun replyTerminalReceive(lines: List<String>) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines.toTypedArray()))
    }

    fun replyTerminalReceive(vararg lines: String) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, lines))
    }

    fun replyTerminalSetLocked(lock: Boolean) {
        reply(ServerActions.SERVER_TERMINAL_RECEIVE, TerminalReceive(TERMINAL_MAIN, emptyArray(), lock))
    }


}
