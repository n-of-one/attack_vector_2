package org.n1.av2.integration.stomp

import org.n1.av2.platform.connection.UNRESTRICTED_ENDPOINT
import org.n1.av2.platform.iam.login.LoginService
import org.n1.av2.platform.iam.user.UserEntityService
import org.springframework.stereotype.Component

/**
 * Connects a STOMP test client through the real login flow: login -> JWT cookie -> websocket handshake.
 */
@Component
class StompClientService(
    private val userEntityService: UserEntityService,
    private val loginService: LoginService,
) {

    var port = 0

    suspend fun connectUser(userName: String): AvClient {
        val user = userEntityService.getByName(userName)
        val cookies = loginService.login(userName, "", "127.0.0.1")
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
