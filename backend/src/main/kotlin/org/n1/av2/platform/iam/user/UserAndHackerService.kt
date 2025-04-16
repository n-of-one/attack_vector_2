package org.n1.av2.platform.iam.user

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.skill.Skill
import org.n1.av2.hacker.skill.SkillService
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
    private val currentUserService: CurrentUserService,
    private val skillService: SkillService,
) {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    enum class UIUserType {
        HACKER,
        GM,
        ADMIN,
        SYSTEM,
    }

    @Suppress("unused")
    class UserOverview(
        val id: String,
        val name: String,
        val characterName: String? = null,
        val hacker: Boolean,
        val type: UIUserType,
        val tag: UserTag,
    )

    fun sendUsersOverview() {
        val allUsers = userEntityService.findAll()
        val allHackers = hackerEntityService.findAll()

        val message = allUsers.map { user ->
            val userType = determineUserType(user)
            if (user.type != UserType.HACKER) return@map UserOverview(user.id, user.name, null, false, userType, user.tag)
            val hacker = allHackers.find { hacker -> hacker.hackerUserId == user.id } ?: error("Hacker not found for user: ${user.name} / ${user.id}")

            UserOverview(user.id, user.name, hacker.characterName, true, userType, user.tag)
        }
        connectionService.reply(ServerActions.SERVER_RECEIVE_USERS_OVERVIEW, message)
    }

    private fun determineUserType(user: UserEntity): UIUserType {
        return when (user.type) {
            UserType.HACKER -> UIUserType.HACKER
            UserType.GM -> UIUserType.GM
            UserType.ADMIN -> UIUserType.ADMIN
            UserType.SYSTEM -> UIUserType.SYSTEM
            else -> error("unsupported user type: ${user.type}")
        }
    }

    fun createFromUserManagementScreen(name: String, externalId: String? = null) {
        val user = userEntityService.createUser(name, UserType.HACKER, externalId)
        createHackerWithDefaultSkill(user, HackerIcon.KOALA)
        sendUsersOverview()
        sendDetailsOfSpecificUser(user.id)
    }

    @Suppress("unused")
    class UiHacker(
        val hackerUserId: String,
        val icon: HackerIcon,
        val characterName: String,
        val skills: List<Skill>,
    )

    class UiUserDetails(
        val id: String,
        val name: String,
        val type: UserType,
        val hacker: UiHacker?,
    )

    fun sendDetailsOfCurrentUser() {
        val uiUserDetails = getDetailsOfSpecificUser(currentUserService.userId)
        connectionService.reply(ServerActions.SERVER_RECEIVE_CURRENT_USER, uiUserDetails)
    }

    fun sendDetailsOfSpecificUser(userId: String) {
        val uiUserDetails = getDetailsOfSpecificUser(userId)
        connectionService.reply(ServerActions.SERVER_RECEIVE_EDIT_USER, uiUserDetails)
        connectionService.toUser(userId, ServerActions.SERVER_RECEIVE_CURRENT_USER, uiUserDetails)
    }

    private fun getDetailsOfSpecificUser(userId: String): UiUserDetails {
        val user = userEntityService.getById(userId)
        val hacker = createHackerForSkillDisplay(user)

        return UiUserDetails(user.id, user.name, user.type, hacker)
    }

    fun createHackerForSkillDisplay(user: UserEntity): UiHacker? {
        val hacker = if (user.type == UserType.HACKER) hackerEntityService.findForUser(user) else return null
        val skills = skillService.findSkillsForUser(user.id)
        val skillsWithDisplayValues = skills.map { skill ->
            if (skill.value != null)
                skill.copy(value = skill.type.toDisplayValue(skill.value))
            else
                skill
        }
        return UiHacker(
            hackerUserId = hacker.hackerUserId,
            icon = hacker.icon,
            characterName = hacker.characterName,
            skills = skillsWithDisplayValues,
        )
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
        sendUsersOverview()
    }

    private fun editUserAttribute(user: UserEntity, field: String, value: String) {
        if (!user.tag.changeable) {
            sendDetailsOfSpecificUser(user.id) // restore correct value in UI
            throw ValidationException("Cannot change ${user.name} because it is ${user.tag.description}.")
        }

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
            connectionService.reply(ServerActions.SERVER_RECEIVE_EDIT_USER, userEntity) // Restore olds values in frontend
            throw ValidationException("User with name $newName already exists.")
        }
        return userEntity.copy(name = newName)
    }

    private fun changeType(user: UserEntity, newTypeName: String): UserEntity {
        val newType = UserType.valueOf(newTypeName)

        if (newType == UserType.HACKER) {
            val existingHacker = hackerEntityService.findForUserOrNull(user)
            if (existingHacker == null) {
                createHackerWithDefaultSkill(user, HackerIcon.BEAR)
            }
        }
        return user.copy(type = newType)
    }

    fun delete(userId: String) {
        if (hackerStateEntityService.isOnline(userId)) throw ValidationException("User is online and cannot be deleted.")
        val user = userEntityService.getById(userId)
        if (!user.tag.changeable) throw ValidationException("Cannot delete ${user.name} because it is ${user.tag.description}.")

        runLinkEntityService.deleteAllForUser(userId)
        userEntityService.delete(userId)
        sendUsersOverview()
        connectionService.replyNeutral("${user.name} deleted")
    }

    fun getOrCreateHackerUser(externalId: String): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(externalId)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        val name = userEntityService.findFreeUserName("user")
        val user = userEntityService.createUser(name, UserType.HACKER, externalId)
        createHackerWithDefaultSkill(user, HackerIcon.FROG)

        return user
    }

    fun createHackerWithDefaultSkill(user: UserEntity, icon: HackerIcon) {
        hackerEntityService.createHacker(user, icon, "not set")
        skillService.createDefaultSkills(user.id)
    }
    }
