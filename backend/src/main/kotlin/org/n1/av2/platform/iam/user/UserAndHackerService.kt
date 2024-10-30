package org.n1.av2.platform.iam.user

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hacker.HackerSkill
import org.n1.av2.hacker.hacker.HackerSkillType
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.run.runlink.RunLinkEntityService
import org.springframework.stereotype.Service

@Service
class UserAndHackerService(
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val connectionService: ConnectionService,
    private val runLinkEntityService: RunLinkEntityService,
) {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    class UserOverview(
        val id: String,
        val name: String,
        val characterName: String? = null,
    )

    fun overview() {
        val allUsers = userEntityService.findAll()
        val allHackers = hackerEntityService.findAll()

        val message = allUsers.map { user ->
            if (user.type != UserType.HACKER) return@map UserOverview(user.id, user.name)
            val hacker = allHackers.find { hacker -> hacker.hackerUserId == user.id } ?: error("Hacker not found for user: ${user.name} / ${user.id}")
            UserOverview(user.id, user.name, hacker.characterName)
        }
        connectionService.reply(ServerActions.SERVER_RECEIVE_USERS_OVERVIEW, message)
    }

    fun createFromUserManagementScreen(name: String, externalId: String? = null) {
        val user = userEntityService.createUser(name, UserType.HACKER, externalId)
        hackerEntityService.createHacker(user.id, HackerIcon.KOALA, "anonymous", defaultSkills())
        overview()
        sendDetailsOfSpecificUser(user.id)
    }

    class UiUserDetails(
        val id: String,
        val name: String,
        val type: UserType,
        val hacker: Hacker?,
    )

    fun sendDetailsOfSpecificUser(userId: String) {
        val user = userEntityService.getById(userId)
        val uiUser = if (user.type == UserType.HACKER) {
            val hacker = hackerEntityService.findForUser(user)
            UiUserDetails(user.id, user.name, user.type, hacker)
        } else {
            UiUserDetails(user.id, user.name, user.type, null)
        }
        connectionService.reply(ServerActions.SERVER_USER_DETAILS, uiUser)
    }

    fun editSkill(userId: String, type: HackerSkillType, add: Boolean) {
        val user = userEntityService.getById(userId)
        if (user.type != UserType.HACKER) throw ValidationException("Only hackers can have skills")

        val hacker = hackerEntityService.findForUser(user)

        val newSkills: List<HackerSkill> = if (add) {
            hacker.skills + HackerSkill(type)
        } else {
            hacker.skills.filterNot { it.type == type }
        }

        val updatedHacker = hacker.copy(skills = newSkills)
        hackerEntityService.save(updatedHacker)

        sendDetailsOfSpecificUser(user.id)
    }

    fun edit(userId: String, field: String, value: String) {
        val user = userEntityService.getById(userId)
        when (field) {
            "name" -> editUserAttribute(user, "name", value)
            "type" -> editUserAttribute(user, "type", value)
            "characterName" -> editHackerAttribute(user, "characterName", value)
            "hackerIcon" -> editHackerAttribute(user, "hackerIcon", value)
            else -> error("Unknown user property: $field")
        }

        sendDetailsOfSpecificUser(user.id)
        overview()
    }

    private fun editUserAttribute(user: UserEntity, field: String, value: String) {
        val editedUser: UserEntity = when (field) {
            "name" -> changeName(user, value)
            "type" -> changeType(user, value)
            else -> error("Unknown user property: $field")
        }
        validate(user.id, editedUser)
        userEntityService.save(editedUser)
    }

    private fun editHackerAttribute(user: UserEntity, field: String, value: String) {
        val hacker = hackerEntityService.findForUser(user)
        val editedHacker: Hacker = when (field) {
            "characterName" -> hacker.copy(characterName = value)
            "hackerIcon" -> hacker.copy(icon = HackerIcon.valueOf(value))
            else -> error("Unknown user property: $field")
        }
        validate(user.id, editedHacker)
        hackerEntityService.save(editedHacker)
    }


    private fun validate(userId: String, toValidate: Any) {
        val violations = validator.validate(toValidate)
        if (violations.isEmpty()) return

        sendDetailsOfSpecificUser(userId) // restore correct value in UI

        val message = violations.joinToString(", ") { it.message }
        throw ValidationException(message)
    }

    private fun changeName(userEntity: UserEntity, newName: String): UserEntity {
        if (userEntity.name == newName) return userEntity
        if (userEntityService.findByNameIgnoreCase(newName) != null) {
            connectionService.reply(ServerActions.SERVER_USER_DETAILS, userEntity) // Restore olds values in frontend
            throw ValidationException("User with name $newName already exists.")
        }
        return userEntity.copy(name = newName)
    }

    private fun changeType(user: UserEntity, newTypeName: String): UserEntity {
        val newType = UserType.valueOf(newTypeName)

        if (newType == UserType.HACKER) {
            hackerEntityService.findForUserOrNull(user) ?: hackerEntityService.createHacker(user.id, HackerIcon.BEAR, "unknown", defaultSkills())
        }
        return user.copy(type = newType)
    }


    val undeletableUserTypes = setOf(UserType.SYSTEM, UserType.ADMIN, UserType.SKILL_TEMPLATE)
    fun delete(userId: String) {
        if (hackerStateEntityService.isOnline(userId)) throw ValidationException("User is online and cannot be deleted.")
        val userEntity = userEntityService.getById(userId)
        if (undeletableUserTypes.contains(userEntity.type)) throw ValidationException("Cannot delete user of type: ${userEntity.type}.")

        runLinkEntityService.deleteAllForUser(userId)
        userEntityService.delete(userId)
        overview()
        connectionService.replyNeutral("User deleted")
    }

    fun getOrCreateHackerUser(externalId: String): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(externalId)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        val name = userEntityService.findFreeUserName("user")
        val user = userEntityService.createUser(name, UserType.HACKER, externalId)
        hackerEntityService.createHacker(
            user.id,
            HackerIcon.FROG,
            "not set",
            defaultSkills()
        )
        return user
    }

    fun replyHackerSkills(user: UserEntity) {
        val hacker = hackerEntityService.findForUser(user)
        connectionService.reply(ServerActions.SERVER_RECEIVE_HACKER_SKILLS, hacker.skills)
    }

    fun defaultSkills(): List<HackerSkill> {
        val template = userEntityService.getByName(TEMPLATE_USER_NAME)
        return hackerEntityService.findForUser(template).skills
    }
}
