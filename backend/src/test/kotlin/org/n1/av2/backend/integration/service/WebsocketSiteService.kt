package org.n1.av2.backend.integration.service

import jakarta.servlet.http.Cookie
import org.n1.av2.backend.entity.run.RunLinkEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.admin.ImportService
import org.n1.av2.backend.service.security.LoginService
import org.springframework.stereotype.Service

@Service
class WebsocketSiteService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val importService: ImportService,
) {

    fun importTestSite(name: String) {
        val siteJson = ClassLoader.getSystemResource(name).readText()
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
    private val sitePropertiesEntityService: SitePropertiesEntityService,
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
