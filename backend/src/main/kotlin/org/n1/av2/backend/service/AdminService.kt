package org.n1.av2.backend.service

import org.n1.av2.backend.entity.ice.IcePasswordStatusRepo
import org.n1.av2.backend.entity.ice.TangleIceStatusRepo
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.HackerStateRepo
import org.n1.av2.backend.entity.run.RunLinkRepo
import org.n1.av2.backend.entity.run.RunRepo
import org.n1.av2.backend.entity.site.ConnectionRepo
import org.n1.av2.backend.entity.site.NodeRepo
import org.n1.av2.backend.entity.site.SiteEditorStateRepo
import org.n1.av2.backend.entity.site.SitePropertiesRepo
import org.n1.av2.backend.entity.user.UserEntityRepo
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.util.StompService
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val hackerStateRepo: HackerStateRepo,
    private val sitePropertiesRepo: SitePropertiesRepo,
    private val nodeRepo: NodeRepo,
    private val connectionRepo: ConnectionRepo,
    private val siteEditorStateRepo: SiteEditorStateRepo,
    private val stompService: StompService,
    private val runRepo: RunRepo,
    private val runLinkRepo: RunLinkRepo,
    private val userEntityService: UserEntityService,
    private val userEntityRepo: UserEntityRepo,
    private val tangleIceStatusRepo: TangleIceStatusRepo,
    private val icePasswordStatusRepo: IcePasswordStatusRepo,
    ) {

    fun purgeAll() {
        sitePropertiesRepo.findAll().forEach { siteProperties ->
            stompService.toSite(siteProperties.siteId, ServerActions.SERVER_FORCE_DISCONNECT, NotyMessage(NotyType.FATAL, "Admin action", "Purging all sites."))
        }

        sitePropertiesRepo.deleteAll()
        nodeRepo.deleteAll()
        connectionRepo.deleteAll()
        siteEditorStateRepo.deleteAll()

        runRepo.deleteAll()
        runLinkRepo.deleteAll()

        hackerStateRepo.deleteAll()
        userEntityRepo.deleteAll()

        tangleIceStatusRepo.deleteAll()
        icePasswordStatusRepo.deleteAll()

        userEntityService.createMandatoryUsers()
        hackerStateEntityService.init()


    }

    fun reset() {
        hackerStateRepo.deleteAll()
        hackerStateEntityService.init()

        tangleIceStatusRepo.deleteAll()
        icePasswordStatusRepo.deleteAll()

        sitePropertiesRepo.findAll().forEach { siteProperties ->
            stompService.toSite(siteProperties.siteId, ServerActions.SERVER_FORCE_DISCONNECT, NotyMessage(NotyType.FATAL, "Admin action", "Purging all sites."))
        }

        hackerStateEntityService.init()

    }
}