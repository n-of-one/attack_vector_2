package org.n1.av2.backend.service

import org.n1.av2.backend.entity.ice.IceStatusRepo
import org.n1.av2.backend.entity.ice.TangleIceStatusRepo
import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserRepo
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val hackerStateEntityService: HackerStateEntityService,
    private val hackerStateRepo: HackerStateRepo,
    private val tracingPatrollerRepo: TracingPatrollerRepo,
    private val sitePropertiesRepo: SitePropertiesRepo,
    private val layoutRepo: LayoutRepo,
    private val nodeRepo: NodeRepo,
    private val connectionRepo: ConnectionRepo,
    private val siteEditorStateRepo: SiteEditorStateRepo,
    private val stompService: StompService,
    private val runRepo: RunRepo,
    private val userRunLinkRepo: UserRunLinkRepo,
    private val userEntityService: UserEntityService,
    private val userRepo: UserRepo,
    private val tangleIceStatusRepo: TangleIceStatusRepo,
    private val iceStatusRepo: IceStatusRepo,
    ) {

    fun purgeAll() {
        tracingPatrollerRepo.deleteAll()

        sitePropertiesRepo.findAll().forEach { siteProperties ->
            stompService.toSite(siteProperties.siteId, ServerActions.SERVER_FORCE_DISCONNECT, NotyMessage(NotyType.FATAL, "Admin action", "Purging all sites."))
        }

        sitePropertiesRepo.deleteAll()
        layoutRepo.deleteAll()
        nodeRepo.deleteAll()
        connectionRepo.deleteAll()
        siteEditorStateRepo.deleteAll()

        runRepo.deleteAll()
        userRunLinkRepo.deleteAll()

        hackerStateRepo.deleteAll()
        userRepo.deleteAll()

        tangleIceStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()

        userEntityService.createMandatoryUsers()
        hackerStateEntityService.init()


    }

    fun reset() {
        tracingPatrollerRepo.deleteAll()
        hackerStateRepo.deleteAll()
        hackerStateEntityService.init()
        tracingPatrollerRepo.deleteAll()

        tangleIceStatusRepo.deleteAll()
        iceStatusRepo.deleteAll()

        sitePropertiesRepo.findAll().forEach { siteProperties ->
            stompService.toSite(siteProperties.siteId, ServerActions.SERVER_FORCE_DISCONNECT, NotyMessage(NotyType.FATAL, "Admin action", "Purging all sites."))
        }

        hackerStateEntityService.init()

    }
}