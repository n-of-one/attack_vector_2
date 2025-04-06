package org.n1.av2.platform.iam.user

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hacker.HackerSkill
import org.n1.av2.hacker.hacker.HackerSkillType.SCAN
import org.n1.av2.hacker.hacker.HackerSkillType.SEARCH_SITE
import org.n1.av2.platform.db.DbSchemaVersioning
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DefaultUserService(
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
) {
    /** Important to be after the ContextRefreshedEvent to give [DbSchemaVersioning] a chance to run first */
    @EventListener(ApplicationStartedEvent::class)
    fun onApplicationEvent() {
        createMandatoryUsers()
    }

    val defaultSkills = listOf(HackerSkill(SEARCH_SITE), HackerSkill(SCAN))

    fun createMandatoryUsers() {
        createDefaultUser("system", UserType.SYSTEM, UserTag.MANDATORY)
        createDefaultUser("admin", UserType.ADMIN, UserTag.MANDATORY)
        createDefaultUser("gm", UserType.GM, UserTag.MANDATORY)

        createSkillTemplate()
    }

    fun createSkillTemplate() {
        createDefaultHacker(TEMPLATE_USER_NAME, HackerIcon.NOT, UserTag.SKILL_TEMPLATE)
    }

    fun createDefaultUser(name: String, type: UserType, role: UserTag = UserTag.REGULAR): UserEntity? {
        val existingUser = userEntityService.findByNameIgnoreCase(name)
        if (existingUser != null) return null // no user created

        return userEntityService.createUser(name, type, null, role)
    }

    fun createDefaultHacker(name: String, icon: HackerIcon, role: UserTag = UserTag.REGULAR): UserEntity? {
        val createdUser = createDefaultUser(name, UserType.HACKER, role)
        if (createdUser == null) return null // no user created, hacker already exists

        val user = userEntityService.findByNameIgnoreCase(name) ?: error("Failed to find user that was just created: $name")

        hackerEntityService.createHacker(user, icon, name, defaultSkills)
        return user
    }

}
