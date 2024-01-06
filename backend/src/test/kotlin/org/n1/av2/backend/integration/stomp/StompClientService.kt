package org.n1.av2.backend.integration.stomp

import org.n1.av2.backend.entity.run.RunLinkEntityService
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.security.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class StompClientService {

    @Autowired
    private lateinit var userEntityService: UserEntityService

    @Autowired
    private lateinit var runLinkEntityService: RunLinkEntityService

    @Autowired
    private lateinit var loginService: LoginService

    private var port = 0

    fun setPort(port: Int) {
        this.port = port
    }

    suspend fun createAndConnect(name: String): HackerClient {
        userEntityService.deleteTestUserIfExists(name)
        runLinkEntityService.deleteAllForUser(name)
        userEntityService.createUserForTest(name)

        val hacker = connectFor(name)
        return hacker
    }

    private suspend fun connectFor(userName: String): HackerClient {
        val user = userEntityService.getByName(userName)
        val cookies = loginService.login(userName, "")
        val cookie = cookies[0]
        val cookieString = "${cookie.name}=${cookie.value}"

        val client = AvClient(userName, port, cookieString, user.id)
        client.connect()
        return HackerClient(client, userName)
    }

}
