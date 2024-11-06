package org.n1.av2.hacker.hacker

import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class HackerEntityService(
    private val hackerRepo: HackerRepo,
) {

    // FIXME: change hackerUserId to user object
    fun createHacker(hackerUserId: String, icon: HackerIcon, characterName: String, skills: List<HackerSkill>) {
        val hacker = Hacker(createHackerId(), hackerUserId, icon, characterName, skills)
        hackerRepo.save(hacker)

    }

    private fun createHackerId(): String {
        fun findExisting(candidate: String): Optional<Hacker> {
            return hackerRepo.findById(candidate)
        }

        return createId("hacker", ::findExisting)
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
