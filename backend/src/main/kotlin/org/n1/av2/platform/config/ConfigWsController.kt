package org.n1.av2.platform.config

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller

@Controller
class ConfigWsController(
    private val configService: ConfigService,
    private val userTaskRunner: UserTaskRunner,
) {

    @MessageMapping("/admin/config/get")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getConfig(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/admin/config/get", userPrincipal) {
            configService.replyConfigValuesAdmin()
        }
    }

    class SetConfigItemCommand(
        val item: ConfigItem,
        val value: String
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @MessageMapping("/admin/config/set")
    fun setConfig(command: SetConfigItemCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/admin/config/set", userPrincipal) {
            configService.setAndReply(command.item, command.value)
        }

    }

}
