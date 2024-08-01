package org.n1.av2.integration.stomp

import org.n1.av2.integration.service.WebsocketUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class StompClientService {

    @Autowired
    private lateinit var websocketUserService: WebsocketUserService

    private var port = 0

    fun setPort(port: Int) {
        this.port = port
    }

    suspend fun createAndConnect(name: String): HackerClient {
        websocketUserService.delete(name)

        val hacker = connectFor(name)
        return hacker
    }

    private suspend fun connectFor(userName: String): HackerClient {
        val (user, cookies) = websocketUserService.login(userName)
        val cookie = cookies[0]
        val cookieString = "${cookie.name}=${cookie.value}"

        val client = AvClient(userName, port, cookieString, user.id)
        client.connect()
        return HackerClient(client, userName)
    }

}
