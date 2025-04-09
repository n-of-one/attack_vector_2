package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import mu.KotlinLogging
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.config.StaticConfig
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.RunService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.export.ImportService
import org.springframework.stereotype.Component

/**
 * V7
 *
 * Detect if the application is running on a development environment and
 * if so: set the configuration for development.
 */
@Component
class V07_DevelopmentDefaults(
    private val staticConfig: StaticConfig,
    private val configService: ConfigService,
    private val currentUserService: CurrentUserService,
    private val userEntityService: UserEntityService,
    private val importService: ImportService,
    private val siteService: SiteService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val runService: RunService,
    private val runLinkService: RunLinkService,
) : MigrationStep {

    override val version = 7

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        if (!staticConfig.developmentEnvironment) {
            return "Detected that this is not a development environment, skipping this step."
        }
        configureForDevelopment()
        val siteName = importDevSite()
        prepareRunLinksForDevSite(siteName)
        return "Configured the system for development."
    }

    private fun importDevSite(): String  {
        try {
            currentUserService.set(userEntityService.getSystemUser())
            val json = this::class.java.getResource("/sites/v1-dev.json")?.readText() ?: error("dev json not found")
            val siteName = importService.importSite(json)
            val siteId = sitePropertiesEntityService.findByName(siteName)?.siteId ?: error("dev site not found after import")
            siteService.updateHackable(siteId, true)
            return siteName
        }
        finally {
            currentUserService.remove()
        }
    }

    private fun prepareRunLinksForDevSite(siteName: String) {
        try {
            currentUserService.set(userEntityService.getByName("hacker"))
            val siteProperties = sitePropertiesEntityService.findByName(siteName) ?: error("dev $siteName not found")
            val run = runService.startNewRun(siteProperties)
            val stalker = userEntityService.getByName("stalker")
            val paradox = userEntityService.getByName("paradox")
            val angler = userEntityService.getByName("angler")

            runLinkService.shareRun(run.runId, stalker, false)
            runLinkService.shareRun(run.runId, paradox, false)
            runLinkService.shareRun(run.runId, angler, false)
        }
        finally {
            currentUserService.remove()
        }

    }



    fun configureForDevelopment() {
        configService.set(ConfigItem.LARP_NAME, "dev")
        configService.set(ConfigItem.LOGIN_PATH, "/devLogin")

        configService.set(ConfigItem.DEV_SIMULATE_NON_LOCALHOST_DELAY_MS, "70")
        configService.set(ConfigItem.DEV_HACKER_RESET_SITE, "true")
        configService.set(ConfigItem.DEV_QUICK_PLAYING, "true")
        configService.set(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS, "true")

        configService.set(ConfigItem.HACKER_SHOW_SKILLS, "true")
        configService.set(ConfigItem.HACKER_EDIT_USER_NAME, "true")
        configService.set(ConfigItem.HACKER_EDIT_CHARACTER_NAME, "true")
        configService.set(ConfigItem.HACKER_TUTORIAL_SITE_NAME, "")
        configService.set(ConfigItem.HACKER_SHOW_SKILLS, "true")

    }


}
