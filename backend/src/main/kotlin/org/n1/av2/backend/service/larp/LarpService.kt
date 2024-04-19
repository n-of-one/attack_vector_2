package org.n1.av2.backend.service.larp

import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.service.larp.frontier.FrontierService
import org.springframework.stereotype.Service

@Service
class LarpService(
    private val serverConfig: ServerConfig,
    private val frontierService: FrontierService,
) {

    fun createMandatoryUsers() {
        if (serverConfig.larp == Larp.FRONTIER) {
            frontierService.createDefaultUsers()
        }
    }

    fun processShare(runId: String, user: UserEntity): Boolean {
        if (serverConfig.larp == Larp.FRONTIER) {
            return frontierService.processShare(runId, user)
        }
        return false
    }

}