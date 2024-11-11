package org.n1.av2.site.tutorial

import org.n1.av2.hacker.hacker.HackerCreatedEvent
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.runlink.RunLinkEntityService
import org.n1.av2.run.scanning.ScanService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.export.ImportService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

const val TUTORIAL_TEMPLATE_NAME = "tutorial"
const val TUTORIAL_INSTANCE_PREFIX = "tutorial-"


@Service
class TutorialService(
    private val importService: ImportService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val siteCloneService: SiteCloneService,
    private val scanService: ScanService,
    private val runEntityService: RunEntityService,
    private val runLinkEntityService: RunLinkEntityService,
    private val currentUserService: CurrentUserService,
    private val userEntityService: UserEntityService,
    private val configService: ConfigService,
    private val connectionService: ConnectionService,

    ) {

    fun importTutorialSite(): Boolean {
        val site = sitePropertiesEntityService.findByName(TUTORIAL_TEMPLATE_NAME)
        if (site != null) return false
        currentUserService.set(userEntityService.getSystemUser())
        val json = this::class.java.getResource("/v3-tutorial.json")?.readText() ?: error("tutorial json not found")
        importService.importSite(json)
        return true
    }

    @EventListener
    fun handleHackerCreated(hackerCreatedEvent: HackerCreatedEvent) {
        val tutorialSiteTemplateName = configService.get(ConfigItem.HACKER_TUTORIAL_SITE_NAME)

        if (tutorialSiteTemplateName.isNotEmpty()) {
            createPersonalTutorialSite(hackerCreatedEvent.user, tutorialSiteTemplateName)
        }
    }

    fun createPersonalTutorialSite(user: UserEntity, tutorialTemplateName: String) {
        val siteName = "${TUTORIAL_INSTANCE_PREFIX}${user.id}"
        val site = sitePropertiesEntityService.findByName(siteName)
        if (site == null) {
            val tutorialSiteProperties = sitePropertiesEntityService.findByName(tutorialTemplateName)

            if (tutorialSiteProperties == null) {
                connectionService.replyError("Tutorial site not found: $tutorialTemplateName, please check the configuration.")
                return
            }

            val siteId = siteCloneService.cloneSite(tutorialSiteProperties, siteName, user)
            if (tutorialSiteProperties.siteStructureOk) {
                val nodeScanById = scanService.createInitialNodeScans(siteId)
                val run = runEntityService.create(siteId, nodeScanById, user.id)
                runLinkEntityService.createRunLink(run.runId, user)
            }
        }
    }
}
