package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.run.RunLinkEntityService
import org.n1.av2.backend.entity.site.SiteEditorStateEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.admin.ImportService
import org.n1.av2.backend.service.run.terminal.scanning.ScanService
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.context.annotation.DependsOn
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
@DependsOn("DbSchemaVersioning")
class TutorialService(
    private val importService: ImportService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val siteCloneService: SiteCloneService,
    private val scanService: ScanService,
    private val runEntityService: RunEntityService,
    private val runLinkEntityService: RunLinkEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val currentUserService: CurrentUserService,
    private val userEntityService: UserEntityService,

    ) {

    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        setup()
    }

    fun setup() {
        val site = sitePropertiesEntityService.findByName("tutorial")
        if (site == null) {
            currentUserService.set(userEntityService.getSystemUser())
            val json = this::class.java.getResource("/v2-tutorial_2.json")?.readText() ?: error("tutorial json not found")
            importService.importSite(json)
        }
    }

    fun createIfNotExistsFor(user: UserEntity) {
        val siteName = "tutorial-${user.id}"
        val site = sitePropertiesEntityService.findByName(siteName)
        if (site == null) {
            val tutorialSiteProperties = sitePropertiesEntityService.findByName("tutorial")!!
            val siteId = siteCloneService.cloneSite(tutorialSiteProperties, siteName)

            if (tutorialSiteProperties.siteStructureOk) {
                val nodeScanById = scanService.createInitialNodeScans(siteId)
                val run = runEntityService.create(siteId, nodeScanById, user.id)
                runLinkEntityService.createRunLink(run.runId, user)
            }
        }
    }
}
