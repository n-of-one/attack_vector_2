package org.n1.av2.larp

import org.n1.av2.platform.config.ServerConfig
import org.n1.av2.platform.iam.login.frontier.FrontierService
import org.n1.av2.platform.iam.user.UserEntity
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
