package org.n1.av2.backend.service

import org.n1.av2.backend.entity.ice.IcePasswordStatusRepo
import org.n1.av2.backend.entity.ice.TangleIceStatusRepo
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.HackerStateRepo
import org.n1.av2.backend.entity.run.RunRepo
import org.n1.av2.backend.entity.run.UserRunLinkRepo
import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserRepo
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
    private val userRunLinkRepo: UserRunLinkRepo,
    private val userEntityService: UserEntityService,
    private val userRepo: UserRepo,
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
        userRunLinkRepo.deleteAll()

        hackerStateRepo.deleteAll()
        userRepo.deleteAll()

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