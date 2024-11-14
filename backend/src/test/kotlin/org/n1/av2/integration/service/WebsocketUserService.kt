package org.n1.av2.integration.service

import jakarta.servlet.http.Cookie
import org.n1.av2.platform.iam.login.LoginService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.springframework.stereotype.Service

@Service
class WebsocketUserService(
    private val userEntityService: UserEntityService,
    private val runLinkEntityService: RunLinkEntityService,
    private val loginService: LoginService,
    private val testUserService: TestUserService,
) {

    suspend fun createTestUser(name: String) {
        testUserService.deleteTestUserIfExists(name)
        runLinkEntityService.deleteAllForUser(name)
        testUserService.createHackerForTest(name)
    }

    suspend fun login(userName: String): Pair<UserEntity, List<Cookie>> {
        val user = userEntityService.getByName(userName)
        val cookies = loginService.login(userName, "", "127.0.0.1")
        return Pair(user, cookies)
    }
}
