package org.n1.av2.gm

import org.n1.av2.hacker.hacker.HackerSkillType
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.inputvalidation.UserName
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Validated
@Controller
class UserWsController(
    private val userTaskRunner: UserTaskRunner,
    private val userAndHackerService: UserAndHackerService

) {

    @MessageMapping("/user/overview")
    fun overview(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userAndHackerService.overview() }
    }

    @MessageMapping("/user/create")
    fun create(@UserName name: String, userPrincipal: UserPrincipal) {

        userTaskRunner.runTask(userPrincipal) { userAndHackerService.createFromUserManagementScreen(name) }
    }

    @MessageMapping("/user/select")
    fun select(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userAndHackerService.sendDetailsOfSpecificUser(userId) }
    }

    class UserEdit(val userId: String, val field: String, val value: String)

    @MessageMapping("/user/edit")
    fun edit(input: UserEdit, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userAndHackerService.edit(input.userId, input.field, input.value) }
    }

    class UserEditSkill(val userId: String, val skill: HackerSkillType, val value: Boolean)

    @MessageMapping("/user/editSkill")
    fun editSkill(input: UserEditSkill, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userAndHackerService.editSkill(input.userId, input.skill, input.value) }
    }

    @MessageMapping("/user/delete")
    fun delete(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { userAndHackerService.delete(userId) }
    }
}
