package org.n1.av2.platform.iam.user

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hacker.HackerSkill
import org.n1.av2.hacker.hacker.HackerSkillType.SCAN
import org.n1.av2.hacker.hacker.HackerSkillType.SEARCH_SITE
import org.n1.av2.larp.LarpService
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DefaultUserService(
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
    private val larpService: LarpService,
) {
    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        createMandatoryUsers()

        larpService.createMandatoryUsers()
    }

    val defaultSkills = listOf(HackerSkill(SEARCH_SITE), HackerSkill(SCAN))

    fun createMandatoryUsers() {
        createDefaultUser("system", UserType.SYSTEM)
        createDefaultUser("admin", UserType.ADMIN)
        createDefaultUser("gm", UserType.GM)

        createSKillTemplate()

        createDefaultHacker("hacker", HackerIcon.CROCODILE)
        createDefaultHacker("Stalker",HackerIcon.BEAR)
        createDefaultHacker("Paradox",HackerIcon.BULL)
        createDefaultHacker("Angler", HackerIcon.SHARK)
    }

    fun createSKillTemplate() {
        val createdUser = createDefaultHacker(TEMPLATE_USER_NAME, HackerIcon.NOT)
        if ( createdUser == null) return

        val editedUserEntity = createdUser.copy(type = UserType.SKILL_TEMPLATE)
        userEntityService.save(editedUserEntity)
    }

    fun createDefaultUser(name: String, type: UserType): UserEntity? {
        val existingUser = userEntityService.findByNameIgnoreCase(name)
        if (existingUser != null) return null // no user created

        return userEntityService.createUser(name, type)
    }

    fun createDefaultHacker(name: String, icon: HackerIcon): UserEntity? {
        val createdUser = createDefaultUser(name, UserType.HACKER)
        if (createdUser == null) return null // no

        val user = userEntityService.findByNameIgnoreCase(name) ?: error("Failed to find user that was just created: $name")

        hackerEntityService.createHacker(user.id, icon, name, defaultSkills)
        return user
    }

}
