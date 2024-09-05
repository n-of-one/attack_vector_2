package org.n1.av2.integration.stomp

import org.n1.av2.integration.service.WebsocketUserService
import org.n1.av2.platform.connection.HACKER_ENDPOINT
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.UNRESTRICTED_ENDPOINT
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

        val hacker = connectHacker(name)
        return hacker
    }

    private suspend fun connectHacker(userName: String): HackerClient {
        val client = connectUser(userName)
        return HackerClient(client, userName)
    }

    suspend fun connectUser(userName: String): AvClient {
        val (user, cookies) = websocketUserService.login(userName)
        val cookie = cookies[0]
        val cookieString = "${cookie.name}=${cookie.value}"

        val client = AvClient(userName, port, cookieString, user.id)
        client.connect()
        return client
    }


    suspend fun connectAnonymous(): AvClient {
        val client = AvClient("", port, "", "", UNRESTRICTED_ENDPOINT)
        client.connect()
        return client
    }

}
