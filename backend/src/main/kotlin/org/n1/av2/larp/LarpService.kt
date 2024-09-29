package org.n1.av2.larp

import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.login.frontier.FrontierService
import org.n1.av2.platform.iam.user.UserEntity
import org.springframework.stereotype.Service

@Service
class LarpService(
    private val configService: ConfigService,
    private val frontierService: FrontierService,
) {

    private fun isFrontier(): Boolean {
        return configService.get(ConfigItem.LARP_NAME).equals("frontier", ignoreCase = true)
    }

    fun createMandatoryUsers() {
        if (isFrontier()) {
            frontierService.createLolaUser()
        }
    }

    fun processShare(runId: String, user: UserEntity): Boolean {
        if (isFrontier()) {
            return frontierService.processShare(runId, user)
        }
        return false
    }

}
