package org.n1.av2.gm

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserService
import org.n1.av2.platform.inputvalidation.UserName
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Validated
@Controller
class UserWsController(
    private val userTaskRunner: UserTaskRunner,
    private val userService: UserService

) {

    @MessageMapping("/user/overview")
    fun overview(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userService.overview() }
    }

    @MessageMapping("/user/create")
    fun create(@UserName name: String, userPrincipal: UserPrincipal) {

        userTaskRunner.runTask(userPrincipal) { userService.createFromScreen(name) }
    }

    @MessageMapping("/user/select")
    fun select(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userService.select(userId) }
    }

    class UserEdit(val userId: String, val field: String, val value: String)

    @MessageMapping("/user/edit")
    fun edit(input: UserEdit, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userService.edit(input.userId, input.field, input.value) }
    }

    @MessageMapping("/user/delete")
    fun delete(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userService.delete(userId) }
    }
}
