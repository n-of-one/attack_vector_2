package org.n1.av2.integration.stomp

import kotlinx.coroutines.delay
import org.n1.av2.platform.connection.HACKER_ENDPOINT
import org.n1.av2.platform.connection.ServerActions
import org.springframework.http.HttpHeaders
import org.springframework.lang.Nullable
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.messaging.support.GenericMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.*


data class ReceivedMessage(
    val type: String,
    val payload: String
)

class AvClient(
    private val name: String,
    private val port: Int,
    private val cookieString: String,
    val userId: String,
    private val endpoint: String = HACKER_ENDPOINT,
) {
    private val receivedMessages = MessageQueue()
    private val sessionHandler = MyStompSessionHandler(receivedMessages, name)
    private lateinit var stompSession: StompSession

    private val logger = mu.KotlinLogging.logger {}

    suspend fun connect() {
        val webSocketClient: WebSocketClient = StandardWebSocketClient()
        val stompClient = WebSocketStompClient(webSocketClient)
        stompClient.defaultHeartbeat = longArrayOf(0, 0)
        stompClient.messageConverter = MyMessageConvertor()

        val url = "ws://localhost:${port}${endpoint}"
        val headers = WebSocketHttpHeaders()
        headers.set(HttpHeaders.COOKIE, cookieString)

        val stompSessionFuture = stompClient.connectAsync(url, headers, sessionHandler)

        this.stompSession = stompSessionFuture.await()

        subscribe("/user/reply")
        subscribe("/topic/user/${sessionHandler.userAndConnectionName}")
        subscribe("/topic/user/${sessionHandler.userName}")
    }


    fun send(destination: String, payload: Any) {
        sessionHandler.send(destination, payload)
    }

    fun subscribe(topic: String) {
        this.sessionHandler.subscribe(topic)
    }

    suspend fun waitFor(action: ServerActions, contents: String, timeoutMillis: Int = 500): String {
        var waitMillis = 0
        logger.debug("start waiting for ${action.name} '${contents}'")
        do {
            val received = receivedMessages.deleteIfContains(ReceivedMessage(action.name, contents))
            if (received != null) {
                return received
            }
            delay(10)
            waitMillis += 10

        } while (waitMillis < timeoutMillis)
        logger.debug("end waiting for ${action.name} '${contents}'")

        receivedMessages.logMessage()
        error("'${name}' times out waiting for ${action.name} '${contents}'")
    }

    suspend fun expectToNotReceive(action: ServerActions, contents: String, timeoutSeconds: Int = 100) {
        try {
            waitFor(action, contents, timeoutSeconds)
            throw AssertionError("Unexpected response from server ${action.name} '${contents}'")
        }
        catch (expected: IllegalStateException) {
            return
        }
    }


    fun clearMessage() {
        receivedMessages.clear()
    }
}


class MyStompSessionHandler(
    receivedMessages: MessageQueue,
    name: String
) : StompSessionHandlerAdapter() {

    val frameHandler = MyStompFrameHander(receivedMessages, name)

    lateinit var session: StompSession

    lateinit var userAndConnectionName: String
    lateinit var userName: String

    fun send(destination: String, payload: Any) {
        session.send(destination, payload)
    }

    override fun afterConnected(incomingSession: StompSession, connectedHeaders: StompHeaders) {
        this.session = incomingSession
        println("New session established : $session")

        userAndConnectionName = connectedHeaders.get("user-name")!![0]
        userName = userAndConnectionName.split("_")[0]
    }

    fun subscribe(destination: String) {
        val headers = StompHeaders()
        headers.set(StompHeaders.ID, "sub-${Date().time}.${System.nanoTime()}")
        headers.set(StompHeaders.DESTINATION, destination)
        session.subscribe(headers, frameHandler)
    }


    override fun handleFrame(headers: StompHeaders, @Nullable payload: Any?) {
        println("Received in session handler: $payload, headers:$headers")
        error("Unexpected receive message not on a subscription")
    }
}

class MyStompFrameHander(
    private val receivedMessages: MessageQueue,
    val name: String
) : StompFrameHandler {

    override fun getPayloadType(headers: StompHeaders): Class<*> {
        return String::class.java
    }

    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        val text = payload as String
        val type = determineType(text)
        receivedMessages.add(ReceivedMessage(type, text))
        println("Client received: [${name}: ${headers.destination!!} : ${text.substring(0, Math.min(250, text.length))}")
    }
}


class MyMessageConvertor : MessageConverter {

    @Nullable
    override fun fromMessage(message: Message<*>, targetClass: Class<*>): Any {
        if (targetClass != String::class.java) {
            error("unsupported message type: ${targetClass}")
        }
        return String(message.payload as ByteArray, Charsets.UTF_8)
    }


    @Nullable
    override fun toMessage(payload: Any, @Nullable headers: MessageHeaders?): Message<*>? {
        if (payload is String) {
            val bytes = payload.toByteArray(Charsets.UTF_8)
            return GenericMessage(bytes, headers!!)
        }
        error("unsupported message type:" + payload.javaClass)
    }
}

fun determineType(json: String): String {
    val type = json.substringAfter("\"type\":\"").substringBefore("\",")
    if (type == json) error("type not found in json: ${json}")

    return type
}
