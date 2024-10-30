package org.n1.av2.platform.iam.user

import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.platform.util.createId
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component("UserEntityService")
class UserEntityService(
    private val userEntityRepo: UserEntityRepo,
) {

    fun findByNameIgnoreCase(userName: String): UserEntity? {
        return userEntityRepo.findByNameIgnoreCase(userName)
    }

    fun getByName(userName: String): UserEntity {
        return findByNameIgnoreCase(userName) ?: throw UsernameNotFoundException("Username not found: ${userName}")
    }

    fun createUserId(): String {
        fun findExisting(candidate: String): Optional<UserEntity> {
            return userEntityRepo.findById(candidate)
        }

        return createId("user", ::findExisting)
    }

    fun getById(userId: String): UserEntity {
        return userEntityRepo.findById(userId).orElseGet { error("${userId} not found") }
    }

    fun getByIdOrNull(userId: String): UserEntity? {
        return userEntityRepo.findById(userId).getOrNull()
    }

    fun searchById(userId: String): UserEntity? {
        return userEntityRepo.findById(userId).getOrNull()
    }

    fun findAll(): List<UserEntity> {
        return userEntityRepo.findAll().toList()
    }

    fun save(userEntity: UserEntity): UserEntity {
        return userEntityRepo.save(userEntity)
    }

    fun delete(userId: String) {
        val user = getById(userId)
        userEntityRepo.delete(user)
    }

    fun findByExternalId(externalId: String): UserEntity? {
        return userEntityRepo.findByExternalId(externalId)
    }


    fun getSystemUser(): UserEntity {
        return userEntityRepo.findByNameIgnoreCase("system") ?: error("System user not found")
    }

    fun createUser(userName: String, type: UserType, externalId: String? = null): UserEntity {
        if (findByNameIgnoreCase(userName) != null) {
            throw ValidationException("User with name $userName already exists.")
        }
        val user = UserEntity(
            id = createUserId(),
            name = userName,
            type = type,
            externalId = externalId,
        )
        save(user)
        return user
    }

    fun findFreeUserName(input: String): String {
        if (findByNameIgnoreCase(input) == null) return input

        for (i in 1..100) {
            val name = "$input$i"
            if (findByNameIgnoreCase(name) == null) return name

        }
        error("Failed to logon, failed to create user account. No free user name found")
    }

    fun getUserName(userId: String): String {
        val user = getByIdOrNull(userId)
        return user?.name ?: "<Deleted>" // if the user is deleted, then we sometimes still need a name.
    }

}
