package org.n1.av2.integration.service

import jakarta.servlet.http.Cookie
import org.n1.av2.platform.iam.login.LoginService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.export.ImportService
import org.springframework.stereotype.Service

@Service
class WebsocketSiteService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val importService: ImportService,
    private val currentUserService: CurrentUserService,
    private val userEntityService: UserEntityService,
) {

    fun importTestSite(name: String) {
        val siteJson = ClassLoader.getSystemResource(name).readText()
        val system = userEntityService.getByName("system")
        currentUserService.set(system)
        importService.importSite(siteJson)
    }

    fun makeHackable(name: String) {
        val properties = sitePropertiesEntityService.findByName(name) ?: error("Site not found: $name")
        val hackableProperties = properties.copy(hackable = true)
        sitePropertiesEntityService.save(hackableProperties)
    }
}

@Service
class WebsocketUserService(
    private val userEntityService: UserEntityService,
    private val runLinkEntityService: RunLinkEntityService,
    private val loginService: LoginService,

    ) {

    suspend fun delete(name: String) {
        userEntityService.deleteTestUserIfExists(name)
        runLinkEntityService.deleteAllForUser(name)
        userEntityService.createUserForTest(name)
    }

    suspend fun login(userName: String): Pair<UserEntity, List<Cookie>> {
        val user = userEntityService.getByName(userName)
        val cookies = loginService.login(userName, "test")
        return Pair(user, cookies)
    }
}
