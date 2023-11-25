package org.n1.av2.backend.integration.stomp

import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.service.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class StompClientService {

    @Autowired
    private lateinit var userEntityService: UserEntityService

    @Autowired
    private lateinit var loginService: LoginService

    private var port = 0

    fun setPort(port: Int) {
        this.port = port
    }

    fun connectHacker(userName: String) : HackerClient {
        val avClient = connectFor(userName)
        return HackerClient(avClient, userName)
    }

    private fun connectFor(userName: String): AvClient {
        val user = userEntityService.getByName(userName)
        val cookies = loginService.login(user)
        val cookie = cookies[0]
        val cookieString = "${cookie.name}=${cookie.value}"


        val avClient = AvClient(userName, port, cookieString, user.id)
        avClient.connect()
        return avClient
    }

    suspend fun createAndConnect(name: String): HackerClient {
        userEntityService.createUser(name, "", UserType.HACKER, HackerIcon.CAT )

        val client = connectHacker(name)
        client.waitForConnection()
        return client
    }

}
