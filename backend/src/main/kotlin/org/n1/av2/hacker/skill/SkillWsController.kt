package org.n1.av2.hacker.skill

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Validated
@Controller
class SkillWsController(
    private val userTaskRunner: UserTaskRunner,
    private val skillService: SkillService,
) {
    class AddSkillCommand(val userId: String, val type: SkillType)
    @MessageMapping("/user/skill/add")
    fun addSkill(command: AddSkillCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/skill/add", userPrincipal) { skillService.addSkill(command.userId, command.type) }
    }

    class EditSkillValueCommand(val skillId: SkillId, val value: String)
    @MessageMapping("/user/skill/edit")
    fun editSkillValue(command: EditSkillValueCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/skill/edit", userPrincipal) { skillService.editSkillValue(command.skillId, command.value) }
    }

    class DeleteSkillCommand(val skillId: SkillId)
    @MessageMapping("/user/skill/delete")
    fun deleteSkill(command: DeleteSkillCommand, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/user/skill/delete", userPrincipal) { skillService.deleteSkill(command.skillId) }
    }

}
