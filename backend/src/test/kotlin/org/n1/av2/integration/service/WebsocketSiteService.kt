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

    fun importTestSite(name: String, owner: String = "hacker") {
        val siteJson = ClassLoader.getSystemResource(name).readText()
        val system = userEntityService.getByName(owner)
        currentUserService.set(system)
        importService.importSite(siteJson)
    }

    fun makeHackable(name: String) {
        val properties = sitePropertiesEntityService.findByName(name) ?: error("Site not found: $name")
        val hackableProperties = properties.copy(hackable = true)
        sitePropertiesEntityService.save(hackableProperties)
    }
}


