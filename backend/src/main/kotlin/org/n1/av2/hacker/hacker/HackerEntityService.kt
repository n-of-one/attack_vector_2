package org.n1.av2.hacker.hacker

import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

class HackerCreatedEvent(val hacker: Hacker, val user: UserEntity)

@Service
class HackerEntityService(
    private val hackerRepo: HackerRepo,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    fun createHacker(user: UserEntity, icon: HackerIcon, characterName: String, skills: List<HackerSkill>) {
        val hackerId = deriveFromUserId(user)
        val hacker = Hacker(hackerId, user.id, icon, characterName, skills)
        hackerRepo.save(hacker)

        val event = HackerCreatedEvent(hacker, user)
        applicationEventPublisher.publishEvent(event)
    }

    private fun deriveFromUserId(user: UserEntity): String {
        return user.id.replace("user", "hacker")
    }

    fun findForUser(user: UserEntity): Hacker {
        verifyUserIsHacker(user)
        return hackerRepo.findByHackerUserId(user.id) ?: error("No hacker entity exists for userId: ${user.name} / ${user.id}")
    }

    fun findForUserOrNull(user: UserEntity): Hacker? {
        return hackerRepo.findByHackerUserId(user.id)
    }

    private fun verifyUserIsHacker(user: UserEntity) {
        if (user.type != UserType.HACKER) error("User ${user.name} / ${user.id} is not a hacker")
    }

    fun findAll(): List<Hacker> {
        return hackerRepo.findAll().toList()
    }

    fun save(hacker: Hacker) {
        hackerRepo.save(hacker)
    }

}
