package org.n1.av2.platform.iam.user

import org.n1.av2.larp.LarpService
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DefaultUserService(
    private val userEntityService: UserEntityService,
    private val larpService: LarpService,
) {
    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        createMandatoryUsers()

        larpService.createMandatoryUsers()
    }

    val defaultSkills = setOf(HackerSkill.SEARCH_SITE, HackerSkill.SCAN)

    fun createMandatoryUsers() {
        createSKillTemplate()
        userEntityService.createDefaultUser("system", UserType.SYSTEM, defaultSkills)
        userEntityService.createDefaultUser("admin", UserType.ADMIN, defaultSkills)
        userEntityService.createDefaultUser("gm", UserType.GM, defaultSkills)
        userEntityService.createDefaultUser("hacker", UserType.HACKER, defaultSkills, HackerIcon.CROCODILE)
        userEntityService.createDefaultUser("Stalker", UserType.HACKER, defaultSkills, HackerIcon.BEAR)
        userEntityService.createDefaultUser("Paradox", UserType.HACKER, defaultSkills, HackerIcon.BULL)
        userEntityService.createDefaultUser("Angler", UserType.HACKER, defaultSkills, HackerIcon.SHARK)
    }

    fun createSKillTemplate() {
        val user = userEntityService.findByNameIgnoreCase(TEMPLATE_USER_NAME)
        if (user != null) return

        userEntityService.createDefaultUser(TEMPLATE_USER_NAME, UserType.HACKER, defaultSkills, HackerIcon.NOT)
        userEntityService.getByName(TEMPLATE_USER_NAME).let {
            val editedUserEntity = it.copy(type = UserType.SKILL_TEMPLATE)
            userEntityService.save(editedUserEntity)
        }
    }

}
