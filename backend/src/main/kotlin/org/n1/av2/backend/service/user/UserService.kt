package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.user.*
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.isInt
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userEntityService: UserEntityService,
    private val stompService: StompService,
    private val hackerStateEntityService: HackerStateEntityService,
) {

    class UserOverview(
        val id: String,
        val name: String,
        val characterName: String?,
        val note: String?
    )

    fun overview() {
        val message = filteredUsers().map { UserOverview(it.id, it.name, it.hacker?.characterName, it.gmNote, ) }
        stompService.reply(ServerActions.SERVER_RECEIVE_USERS_OVERVIEW, message)
    }

    private fun filteredUsers(): List<User> {
        val all = userEntityService.findAll()
        if (!SecurityContextHolder.getContext().authentication.authorities.contains(ROLE_USER_MANAGER)) {
            return all.filter { it.type == UserType.HACKER || it.type == UserType.HACKER_MANAGER }
        }
        return all
    }

    fun createFromScreen(name: String, externalId: String? = null) {
        val user = create(name, externalId)
        overview()
        select(user.id)
    }

    fun create(name: String, externalId: String? = null, type: UserType = UserType.HACKER, hacker: Hacker? = null): User {
        if (userEntityService.findByNameIgnoreCase(name) != null) {
            throw ValidationException("User with name $name already exists.")
        }
        val userId = userEntityService.createUserId()
        val userInput = User(
            id = userId,
            externalId = externalId,
            email = "",
            name = name,
            type = type,
            hacker = hacker
        )
        return userEntityService.save(userInput)
    }

    fun select(userId: String) {
        val user = userEntityService.getById(userId)
        stompService.reply(ServerActions.SERVER_USER_DETAILS, user)
    }

    fun edit(userId: String, field: String, value: String) {
        val user = userEntityService.getById(userId)
        val editedUser: User = when (field) {
            "name" -> user.copy(name = value)
            "email" -> user.copy(email = value)
            "type" -> changeType(user, value)
            "gmNote" -> user.copy(gmNote = value)
            "characterName" -> user.copy(hacker = user.hacker?.copy(characterName = value))
            "hackerIcon" -> user.copy(hacker = user.hacker?.copy(icon = HackerIcon.valueOf(value)))
            "skillHacker" -> changeSkill(user, field, value)
            "skillElite" -> changeSkill(user, field, value)
            "skillArchitect" -> changeSkill(user, field, value)
            else -> error("Unknown user property: $field")
        }
        userEntityService.save(editedUser)
        stompService.reply(ServerActions.SERVER_USER_DETAILS, editedUser)
        overview()
    }

    private fun changeType(user: User, newType: String): User {
        if (newType != UserType.HACKER.name) {
            return user.copy(type = UserType.valueOf(newType))
        }
        if (user.hacker != null) {
            return user.copy(type = UserType.valueOf(newType))
        }
        val newHacker = Hacker(HackerIcon.BEAR, HackerSkill(1, 0, 0), "anonymous")
        return user.copy(type = UserType.valueOf(newType), hacker = newHacker)
    }

    private fun changeSkill(user: User, field: String, value: String): User {
        val skill = user.hacker?.skill ?: error("User is not a hacker")
        if (!value.isInt()) throw ValidationException("Please enter a whole number for skill value: $field")
        val newSkillValue = value.toInt()
        if (newSkillValue < 0 || newSkillValue > 5) throw ValidationException("Skill value must be between 0 and 5: $field")

        val newSkill = when (field) {
            "skillHacker" -> skill.copy(hacker = newSkillValue)
            "skillElite" -> skill.copy(elite = newSkillValue)
            "skillArchitect" -> skill.copy(architect = newSkillValue)
            else -> error("Unknown skill property: $field")
        }
        val newHacker = user.hacker.copy(skill = newSkill)
        return user.copy(hacker = newHacker)
    }

    fun delete(userId: String) {
        if (hackerStateEntityService.isOnline(userId)) throw ValidationException("User is online and cannot be deleted.")

        userEntityService.delete(userId)
        overview()
        stompService.replyNeutral("User deleted")
    }

    fun update(updatedUser: User): User {
        return userEntityService.save(updatedUser)
    }

}