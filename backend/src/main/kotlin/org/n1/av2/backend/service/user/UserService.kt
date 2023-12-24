package org.n1.av2.backend.service.user

import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.entity.run.RunLinkEntityService
import org.n1.av2.backend.entity.user.*
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.isInt
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userEntityService: UserEntityService,
    private val stompService: StompService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runLinkEntityService: RunLinkEntityService,
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

    private fun filteredUsers(): List<UserEntity> {
        val all = userEntityService.findAll()
        if (!SecurityContextHolder.getContext().authentication.authorities.contains(ROLE_USER_MANAGER)) {
            return all.filter { it.type == UserType.HACKER || it.type == UserType.HACKER_MANAGER }
        }
        return all
    }

    fun createFromScreen(name: String, externalId: String? = null) {
        val hacker = Hacker(HackerIcon.KOALA, HackerSkill(1,0,0), "anonymous")
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
            email = "",
            name = name,
            type = type,
            hacker = hacker,

        )
        return userEntityService.save(userEntityInput)
    }

    fun select(userId: String) {
        val user = userEntityService.getById(userId)
        stompService.reply(ServerActions.SERVER_USER_DETAILS, user)
    }

    fun edit(userId: String, field: String, value: String) {
        val user = userEntityService.getById(userId)
        val editedUserEntity: UserEntity = when (field) {
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
        userEntityService.save(editedUserEntity)
        stompService.reply(ServerActions.SERVER_USER_DETAILS, editedUserEntity)
        overview()
    }

    private fun changeType(userEntity: UserEntity, newType: String): UserEntity {
        if (newType != UserType.HACKER.name) {
            return userEntity.copy(type = UserType.valueOf(newType))
        }
        if (userEntity.hacker != null) {
            return userEntity.copy(type = UserType.valueOf(newType))
        }
        val newHacker = Hacker(HackerIcon.BEAR, HackerSkill(1, 0, 0), "anonymous")
        return userEntity.copy(type = UserType.valueOf(newType), hacker = newHacker)
    }

    private fun changeSkill(userEntity: UserEntity, field: String, value: String): UserEntity {
        val skill = userEntity.hacker?.skill ?: error("User is not a hacker")
        if (!value.isInt()) throw ValidationException("Please enter a whole number for skill value: $field")
        val newSkillValue = value.toInt()
        if (newSkillValue < 0 || newSkillValue > 5) throw ValidationException("Skill value must be between 0 and 5: $field")

        val newSkill = when (field) {
            "skillHacker" -> skill.copy(hacker = newSkillValue)
            "skillElite" -> skill.copy(elite = newSkillValue)
            "skillArchitect" -> skill.copy(architect = newSkillValue)
            else -> error("Unknown skill property: $field")
        }
        val newHacker = userEntity.hacker.copy(skill = newSkill)
        return userEntity.copy(hacker = newHacker)
    }

    fun delete(userId: String) {
        if (hackerStateEntityService.isOnline(userId)) throw ValidationException("User is online and cannot be deleted.")

        runLinkEntityService.deleteAllForUser(userId)
        userEntityService.delete(userId)
        overview()
        stompService.replyNeutral("User deleted")
    }

    fun update(updatedUserEntity: UserEntity): UserEntity {
        return userEntityService.save(updatedUserEntity)
    }

}