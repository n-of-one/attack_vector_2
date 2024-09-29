package org.n1.av2.platform.iam.user

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.login.frontier.FrontierHackerInfo
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.run.runlink.RunLinkEntityService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userEntityService: UserEntityService,
    private val connectionService: ConnectionService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runLinkEntityService: RunLinkEntityService,
) {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    class UserOverview(
        val id: String,
        val name: String,
        val characterName: String?,
    )

    fun overview() {
        val message = filteredUsers().map { UserOverview(it.id, it.name, it.hacker?.characterName ) }
        connectionService.reply(ServerActions.SERVER_RECEIVE_USERS_OVERVIEW, message)
    }

    private fun filteredUsers(): List<UserEntity> {
        val all = userEntityService.findAll()
        if (!SecurityContextHolder.getContext().authentication.authorities.contains(ROLE_USER_MANAGER)) {
            return all.filter { it.type == UserType.HACKER }
        }
        return all
    }

    fun createFromScreen(name: String, externalId: String? = null) {
        val hacker = Hacker(HackerIcon.KOALA, "anonymous", defaultSkills)
        val user = create(name, externalId, UserType.HACKER, hacker)
        overview()
        select(user.id)
    }

    fun create(name: String, externalId: String? = null, type: UserType, hacker: Hacker?): UserEntity {
        if (userEntityService.findByNameIgnoreCase(name) != null) {
            throw ValidationException("User with name $name already exists.")
        }
        val userId = userEntityService.createUserId()
        val userEntityInput = UserEntity(
            id = userId,
            externalId = externalId,
            name = name,
            type = type,
            hacker = hacker,

        )
        return userEntityService.save(userEntityInput)
    }

    fun select(userId: String) {
        val user = userEntityService.getById(userId)
        connectionService.reply(ServerActions.SERVER_USER_DETAILS, user)
    }

    fun editSkill(userId: String, skill: HackerSkill, add: Boolean) {
        val user = userEntityService.getById(userId)
        if (user.hacker == null) throw ValidationException("Only hackers can have skills")

        val newSkills = if (add) {
            (user.hacker.skills ?: defaultSkills) + skill
        } else {
            (user.hacker.skills ?: defaultSkills) - skill
        }

        val editedUserEntity = user.copy(hacker = user.hacker.copy(skills = newSkills))
        userEntityService.save(editedUserEntity)

        connectionService.reply(ServerActions.SERVER_USER_DETAILS, editedUserEntity)
    }

    fun edit(userId: String, field: String, value: String) {
        val user = userEntityService.getById(userId)
        val editedUserEntity: UserEntity = when (field) {
            "name" -> changeName(user, value)
            "type" -> changeType(user, value)
            "characterName" -> user.copy(hacker = user.hacker?.copy(characterName = value))
            "hackerIcon" -> user.copy(hacker = user.hacker?.copy(icon = HackerIcon.valueOf(value)))
            else -> error("Unknown user property: $field")
        }
        validate(editedUserEntity, user)

        userEntityService.save(editedUserEntity)
        connectionService.reply(ServerActions.SERVER_USER_DETAILS, editedUserEntity)
        overview()
    }

    protected fun validate(editedUserEntity: UserEntity, user: UserEntity) {
        val violations = validator.validate(editedUserEntity)
        if (violations.isEmpty()) return

        connectionService.reply(ServerActions.SERVER_USER_DETAILS, user) // Restore olds values in frontend

        val message = violations.joinToString(", ") { it.message }
        throw ValidationException(message)
    }

    private fun changeName(userEntity: UserEntity, newName: String): UserEntity {
        if (userEntity.name == newName) return userEntity
        if (userEntityService.findByNameIgnoreCase(newName) != null) {
            connectionService.reply(ServerActions.SERVER_USER_DETAILS, userEntity) // Restore olds values in frontend
            throw ValidationException("User with name $newName already exists.")
        }
        val newEntity = userEntity.copy(name = newName)
        validate(newEntity, userEntity)
        return newEntity
    }

    private fun changeType(userEntity: UserEntity, newType: String): UserEntity {
        if (newType != UserType.HACKER.name) {
            return userEntity.copy(type = UserType.valueOf(newType))
        }
        if (userEntity.hacker != null) {
            return userEntity.copy(type = UserType.valueOf(newType))
        }
        val newHacker = Hacker(HackerIcon.BEAR, "anonymous", defaultSkills)
        return userEntity.copy(type = UserType.valueOf(newType), hacker = newHacker)
    }



    fun delete(userId: String) {
        if (hackerStateEntityService.isOnline(userId)) throw ValidationException("User is online and cannot be deleted.")
        val userEntity = userEntityService.getById(userId)
        if (userEntity.type == UserType.SYSTEM) throw ValidationException("Cannot delete system user.")
        if (userEntity.type == UserType.ADMIN) throw ValidationException("Cannot delete admin user.")

        runLinkEntityService.deleteAllForUser(userId)
        userEntityService.delete(userId)
        overview()
        connectionService.replyNeutral("User deleted")
    }

    fun update(updatedUserEntity: UserEntity): UserEntity {
        return userEntityService.save(updatedUserEntity)
    }

    fun getOrCreateUser(hackerInfo: FrontierHackerInfo): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(hackerInfo.id)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        if (hackerInfo.isGm) {
            return create(hackerInfo.id, hackerInfo.id, UserType.GM, null)
        }

        val name = findFreeUserName(hackerInfo.characterName!!)

        val hacker = Hacker(
            icon = HackerIcon.FROG,
            characterName = "not yet set",
            defaultSkills
        )

        return create(name, hackerInfo.id, UserType.HACKER, hacker)
    }

    fun updateUserInfo(userEntity: UserEntity, hackerInfo: FrontierHackerInfo): UserEntity {
        if (hackerInfo.isGm) return userEntity

        val hacker = userEntity.hacker!!

        val updatedHacker = hacker.copy(
            characterName = hackerInfo.characterName!!,
        )
        val updatedUser = userEntity.copy(hacker = updatedHacker)
        return update(updatedUser)
    }

    private fun findFreeUserName(input: String): String {
        if (userEntityService.findByNameIgnoreCase(input) == null) return input

        for (i in 1..100) {
            val name = "$input$i"
            if (userEntityService.findByNameIgnoreCase(name) == null) return name

        }
        error("Failed to logon, failed to create user account. No free user name found")
    }

    fun getOrCreateUser(externalId: String): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(externalId)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        val name = findFreeUserName("user")

        val hacker = Hacker(
            icon = HackerIcon.FROG,
            characterName = "not yet set",
            defaultSkills
        )

        return create(name, externalId, UserType.HACKER, hacker)
    }
}
