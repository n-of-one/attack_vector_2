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
import org.n1.av2.script.ScriptAccess
import org.n1.av2.script.ScriptAccessService
import org.n1.av2.script.ScriptService
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class UserAndHackerService(
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val connectionService: ConnectionService,
    private val runLinkEntityService: RunLinkEntityService,
    private val scriptService: ScriptService,
    private val scriptAccessService: ScriptAccessService,
    private val currentUserService: CurrentUserService,
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
        hackerEntityService.createHacker(user, HackerIcon.KOALA, "anonymous", defaultSkills())
        overview()
        sendDetailsOfSpecificUser(user.id)
    }

    class UiScript(
        val code: String,
        val typeId: String,
        val timeLeft: String,
        val usable: Boolean,
    )

    class UiHacker(
        val hackerUserId: String,
        val icon: HackerIcon,
        val characterName: String,
        val skills: List<HackerSkill>,
        val scripts: List<UiScript>,
        val scriptAccess: List<ScriptAccess>
    )

    class UiUserDetails(
        val id: String,
        val name: String,
        val type: UserType,
        val hacker: UiHacker?,
    )

    fun sendDetailsOfCurrentUser() {
        val millis = measureTimeMillis {
            val uiUserDetails = getDetailsOfSpecificUser(currentUserService.userId)
            connectionService.reply(ServerActions.SERVER_RECEIVE_CURRENT_USER, uiUserDetails)
        }
        println("sendDetailsOfSpecificUser took $millis ms")
    }

    fun sendDetailsOfSpecificUser(userId: String) {
        val millis = measureTimeMillis {
            val uiUserDetails = getDetailsOfSpecificUser(userId)
            connectionService.reply(ServerActions.SERVER_RECEIVE_EDIT_USER, uiUserDetails)
        }
        println("sendDetailsOfSpecificUser took $millis ms")
    }

    private fun getDetailsOfSpecificUser(userId: String): UiUserDetails {
        val user = userEntityService.getById(userId)
        val scripts = scriptService.findScriptsForUser(userId)
            .map { script ->
                val timeLeft = scriptService.timeLeft(script)
                val usable = scriptService.usable(script)
                UiScript(script.code, script.typeId, timeLeft, usable)
            }
        val scriptAccess = scriptAccessService.findScriptAccessForUser(userId)
        val hacker = createHackerForSkillDisplay(user, scripts, scriptAccess)


        return UiUserDetails(user.id, user.name, user.type, hacker)
    }

    fun createHackerForSkillDisplay(user: UserEntity, uiScripts: List<UiScript>, scriptAccess: List<ScriptAccess>): UiHacker? {
        val hacker = if (user.type == UserType.HACKER) hackerEntityService.findForUser(user) else return null
        val skillsWithDisplayValues = hacker.skills.map { skill ->
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
            scripts = uiScripts,
            scriptAccess = scriptAccess
        )
    }

    fun editSkillEnabled(userId: String, type: HackerSkillType, add: Boolean) {
        val (user, hacker) = retrieveUserAndHacker(userId)

        val newSkills: List<HackerSkill> = if (add) {
            hacker.skills + HackerSkill(type)
        } else {
            hacker.skills.filterNot { it.type == type }
        }

        updateSkillsAndNotifyFrontend(hacker, newSkills, user)
    }

    fun editSkillValue(userId: String, type: HackerSkillType, valueInput: String) {
        validateSKillValue(userId, type, valueInput)

        val value = type.toFunctionalValue(valueInput)
        val (user, hacker) = retrieveUserAndHacker(userId)
        val skill = hacker.skills.find { it.type == type } ?: error("Skill not found: $type")
        val updatedSkill = skill.copy(value = value)
        val newSkills = hacker.skills.map { if (it.type == type) updatedSkill else it }

        updateSkillsAndNotifyFrontend(hacker, newSkills, user)
    }

    private fun validateSKillValue(userId: String, type: HackerSkillType, valueInput: String) {
        val validationFunction = type.validate ?: return // no validation function
        val errorMessage = validationFunction(valueInput) ?: return // no error

        sendDetailsOfSpecificUser(userId)
        throw ValidationException(errorMessage)
    }

    private fun retrieveUserAndHacker(userId: String): Pair<UserEntity, Hacker> {
        val user = userEntityService.getById(userId)
        if (user.type != UserType.HACKER) throw ValidationException("Only hackers can have skills")
        val hacker = hackerEntityService.findForUser(user)
        return Pair(user, hacker)
    }

    private fun updateSkillsAndNotifyFrontend(
        hacker: Hacker,
        newSkills: List<HackerSkill>,
        user: UserEntity
    ) {
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
            hackerEntityService.findForUserOrNull(user) ?: hackerEntityService.createHacker(user, HackerIcon.BEAR, "unknown", defaultSkills())
        }
        return user.copy(type = newType)
    }

    fun delete(userId: String) {
        if (hackerStateEntityService.isOnline(userId)) throw ValidationException("User is online and cannot be deleted.")
        val user = userEntityService.getById(userId)
        if (!user.tag.changeable) throw ValidationException("Cannot delete ${user.name} because it is ${user.tag.description}.")

        runLinkEntityService.deleteAllForUser(userId)
        userEntityService.delete(userId)
        overview()
        connectionService.replyNeutral("${user.name} deleted")
    }

    fun getOrCreateHackerUser(externalId: String): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(externalId)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        val name = userEntityService.findFreeUserName("user")
        val user = userEntityService.createUser(name, UserType.HACKER, externalId)
        hackerEntityService.createHacker(
            user,
            HackerIcon.FROG,
            "not set",
            defaultSkills()
        )
        return user
    }

    fun defaultSkills(): List<HackerSkill> {
        val template = userEntityService.getByName(TEMPLATE_USER_NAME)
        return hackerEntityService.findForUser(template).skills
    }
}
