package org.n1.av2.platform.config

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class ConfigWsController(
    private val configService: ConfigService,
    private val userTaskRunner: UserTaskRunner,
) {

    @MessageMapping("/admin/config/get")
    fun getConfig(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/admin/config/get", userPrincipal) {
            configService.replyConfigValues()
        }
    }

    class SetConfigItemCommand(
        val item: ConfigItem,
        val value: String
    )
    @MessageMapping("/admin/config/set")
    fun setConfig(command: SetConfigItemCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/admin/config/set", userPrincipal) {
            configService.setAndReply(command.item, command.value)
        }

    }

}
