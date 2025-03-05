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
        userTaskRunner.runTask("/user/overview", userPrincipal) { userAndHackerService.overview() }
    }

    @MessageMapping("/user/create")
    fun create(@UserName name: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/create", userPrincipal) { userAndHackerService.createFromUserManagementScreen(name) }
    }

    @MessageMapping("/user/select")
    fun select(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/select", userPrincipal) { userAndHackerService.sendDetailsOfSpecificUser(userId) }
    }

    class UserEdit(val userId: String, val field: String, val value: String)

    @MessageMapping("/user/edit")
    fun edit(input: UserEdit, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/edit", userPrincipal) { userAndHackerService.edit(input.userId, input.field, input.value) }
    }

    class UserEditSkillEnabled(val userId: String, val type: HackerSkillType, val enabled: Boolean)
    @MessageMapping("/user/editSkillEnabled")
    fun editSkillEnabled(input: UserEditSkillEnabled, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/editSkillEnabled", userPrincipal) { userAndHackerService.editSkillEnabled(input.userId, input.type, input.enabled) }
    }

    class UserEditSkillValue(val userId: String, val type: HackerSkillType, val value: String)
    @MessageMapping("/user/editSkillValue")
    fun editSkillValue(input: UserEditSkillValue, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/editSkillValue", userPrincipal) { userAndHackerService.editSkillValue(input.userId, input.type, input.value) }
    }

    @MessageMapping("/user/delete")
    fun delete(userId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/delete", userPrincipal) { userAndHackerService.delete(userId) }
    }
}
