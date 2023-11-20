package org.n1.av2.backend.service.layerhacking

import org.junit.jupiter.api.Test
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.lang.Nullable
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.*
import kotlin.text.Charsets.UTF_8


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebsocketTest(
    ){

    @Autowired private lateinit var userEntityService: UserEntityService
    @Autowired private lateinit var loginService: LoginService


    @Value(value = "\${local.server.port}")
    private val port = 0

    @Test
    fun testWebSocketCommunication() {

        val user = userEntityService.getByName("paradox")
        val cookies = loginService.login(user)
        val cookie = cookies[0]
        val cookieString="${cookie.name}=${cookie.value}"



        val webSocketClient: WebSocketClient = StandardWebSocketClient()
        val stompClient = WebSocketStompClient(webSocketClient)
        stompClient.defaultHeartbeat = longArrayOf(0, 0)
        stompClient.setMessageConverter(MyMessageConvertor())


        val url = "ws://localhost:${port}/ws_hacker"
        val sessionHandler: StompSessionHandler = MyStompSessionHandler()
        val headers = WebSocketHttpHeaders()
        headers.set(HttpHeaders.COOKIE, cookieString)

        stompClient.start()

        println("running:" + stompClient.isRunning)
        stompClient.connect(url, headers, sessionHandler)
        println("running:" + stompClient.isRunning)
        Thread.sleep(3000)
        println("running:" + stompClient.isRunning)
        Thread.sleep(1000)



    }
}


class MyStompSessionHandler : StompSessionHandlerAdapter() {

    val frameHandler = MyStompFrameHander();

    override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
        println("New session established : $session")

        val userAndConnectionName = connectedHeaders.get("user-name")!![0]
        val userName = userAndConnectionName.split("_")[0]

        val headers = StompHeaders()
        headers.set(StompHeaders.ID, "sub-${Date().time}.123")
        headers.set(StompHeaders.DESTINATION, "/user/reply")

        subscribe(session, "/user/reply")
        subscribe(session, "/topic/user/${userAndConnectionName}")
        subscribe(session, "/topic/user/${userName}")

//        session.subscribe("/user/reply", frameHandler)
//        session.subscribe("/topic/user/${userAndConnectionName}", frameHandler)
//        session.subscribe("/topic/user/${userName}", frameHandler)
        session.send("/av/scan/scansOfPlayer", "")

    }

    private fun subscribe(session: StompSession, destination: String) {
        val headers = StompHeaders()
        headers.set(StompHeaders.ID, "sub-${Date().time}.${System.nanoTime()}")
        headers.set(StompHeaders.DESTINATION, destination)
        session.subscribe(headers, frameHandler)
    }


    override fun handleFrame(headers: StompHeaders, @Nullable payload: Any?) {
        println("Received : $payload, headers:$headers")

    }
}

class MyStompFrameHander: StompFrameHandler {
    override fun getPayloadType(headers: StompHeaders): Class<*> {
        return String::class.java
    }

    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        println("Received : $payload, headers:$headers")
    }
}

class MyMessageConvertor: MessageConverter {

    @Nullable
    override fun fromMessage(message: Message<*>, targetClass: Class<*>): Any? {
        println("fromMessage: ${message}")

        val payload = message.payload
        val body = String(payload as ByteArray, UTF_8)
        println("body: ${body}")

        return null
    }


    @Nullable
    override fun toMessage(payload: Any, @Nullable headers: MessageHeaders?): Message<*>? {
        val contentType = headers!!.get(MessageHeaders.CONTENT_TYPE)
        println("Content-type: ${contentType}")
        return null
    }



}