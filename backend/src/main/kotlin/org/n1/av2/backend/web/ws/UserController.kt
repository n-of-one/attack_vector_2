package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.validation.UserName
import org.n1.av2.backend.service.user.UserService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Validated
@Controller
class UserController(
    private val taskRunner: TaskRunner,
    private val userService: UserService

) {

    @MessageMapping("/user/overview")
    fun overview(userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { userService.overview() }
    }

    @MessageMapping("/user/create")
    fun create(@UserName name: String, userPrincipal: UserPrincipal) {

        taskRunner.runTask(userPrincipal) { userService.create(name) }
    }

    @MessageMapping("/user/select")
    fun select(userId: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { userService.select(userId) }
    }

    class UserEdit(val userId: String, val field: String, val value: String)

    @MessageMapping("/user/edit")
    fun edit(input: UserEdit, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { userService.edit(input.userId, input.field, input.value) }
    }

    @MessageMapping("/user/delete")
    fun delete(userId: String, userPrincipal: UserPrincipal) {
        taskRunner.runTask(userPrincipal) { userService.delete(userId) }
    }
}