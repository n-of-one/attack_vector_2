package org.n1.av2.hacker.skill

import org.n1.av2.platform.iam.user.*
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.platform.util.createId
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Configuration
class InitSkillService(
    userAndHackerService: UserAndHackerService,
    skillService: SkillService,
) {
    init {
        skillService.userAndHackerService = userAndHackerService
    }
}

@Service
class SkillService(
    private val currentUserService: CurrentUserService,
    private val skillRepo: SkillRepo,
    private val applicationContext: ApplicationContext,
    private val userEntityService: UserEntityService,
) {
    lateinit var userAndHackerService: UserAndHackerService

    fun getSkill(skillId: SkillId): Skill {
        return skillRepo.findById(skillId).orElse(null) ?: error("Skill with id $skillId not found")
    }

    fun currentUserHasSkill(type: SkillType): Boolean {
        val skills = skillRepo.findByUserId(currentUserService.userId)
        return skills.any { it.type == type }
    }

    fun findSkillsForUser(userId: String): List<Skill> {
        return skillRepo.findByUserId(userId)
    }

    fun skillAsIntOrNull(userId: String, type: SkillType): Int? {
        val matchingSkills = findSkillsForUser(userId).filter { it.type == type }
        val values = matchingSkills.mapNotNull { it.value?.toIntOrNull() }
        return values.maxOrNull()
    }

    fun addSkill(userId: String, type: SkillType) {
        val id = createId("skill", skillRepo::findById)
        val skill = Skill(id, userId, type)
        skillRepo.save(skill)

        userAndHackerService.sendDetailsOfSpecificUser(userId)
    }


    fun editSkillValue(skillId: SkillId, valueInput: String) {
        val skill = getSkill(skillId)

        validateSkillValue(skill.userId, skill.type, valueInput)

        val value = skill.type.toFunctionalValue(valueInput)
        val updatedSkill = skill.copy(value = value)
        skillRepo.save(updatedSkill)
        skill.type.processUpdate(skill.userId, value, applicationContext)
        userAndHackerService.sendDetailsOfSpecificUser(skill.userId)
    }


    fun deleteSkill(skillId: SkillId) {
        val skill = getSkill(skillId)

        skillRepo.delete(skill)
        skill.type.processSkillRemoval(skill.userId, applicationContext)
        userAndHackerService.sendDetailsOfSpecificUser(skill.userId)
    }


    private fun validateSkillValue(userId: String, type: SkillType, valueInput: String) {
        val validationFunction = type.validate ?: return // no validation function
        val errorMessage = validationFunction(valueInput) ?: return // no error
        userAndHackerService.sendDetailsOfSpecificUser(userId) // restore previous valid value
        throw ValidationException(errorMessage)
    }

    fun createDefaultSkills(userId: String) {
        val template = userEntityService.getByName(TEMPLATE_USER_NAME)
        val defaultSkills = findSkillsForUser(template.id)

        defaultSkills.forEach { templateSkill ->
            val skill = templateSkill.copy(
                id = createId("skill", skillRepo::findById),
                userId = userId,
                usedOnSiteIds = emptyList()
            )
            skillRepo.save(skill)
        }
    }

    fun addSkillsForUser(user: UserEntity, types: List<SkillType>) {
        types.forEach { type ->
            val id = createId("skill", skillRepo::findById)
            val skill = Skill(id, user.id, type)
            skillRepo.save(skill)
        }
    }

    fun useSkillOnSite(skillId: SkillId, siteId: String) {
        val skill = getSkill(skillId)
        val updatedSkill = skill.copy(usedOnSiteIds = skill.usedOnSiteIds + siteId)
        skillRepo.save(updatedSkill)
    }

    fun resetSite(siteId: String) {
        val allSkills = skillRepo.findAll()
        allSkills
            .filter { skill -> skill.usedOnSiteIds.contains(siteId) }
            .forEach { skill ->
                val updatedSkill = skill.copy(usedOnSiteIds = skill.usedOnSiteIds - siteId)
                skillRepo.save(updatedSkill)
            }
    }

}
