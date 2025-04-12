package org.n1.av2.platform.iam.login.frontier

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.Skill
import org.n1.av2.hacker.skill.SkillType.*
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.springframework.stereotype.Service

const val FRONTIER_GM_GROUP = "30"

class FrontierUserAndCharacterInfo(
    val frontierExternalReference: String,
    val isGm: Boolean,
    val characterId: String? = null,
    val characterName: String? = null,
)

class FrontierUserAndGroupInfo(
    val accountId: String,
    val gm: Boolean
)

/**
 * SSO for Frontier is based on cookies.
 * Because AV for Frontier is running on av.eosfrontier.space, and the authentication cookie is for the .eosfrontier.space domain,
 * the frontend will send this cookie with every request.
 *
 * Using this cookie, the backend can retrieve the user's id and group memberships as if it were the user.
 *
 * Further information is then retrieved from Orthank (api.eosfrontier.space) to get player and character info.
 *
 * Impersonating the user from the backend, using the user cookie is a bit unorthodox, but it prevents CORS issues.
 * CORS is much more restrictive than shared domain cookies.
 */
@Service
class FrontierService(
    private val orthankService: OrthankService,
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
) {


    private val LEVEL_1_SKILLS = listOf(Skill(SCAN), Skill(SEARCH_SITE) )
    private val LEVEL_3_SKILLS = LEVEL_1_SKILLS + Skill(CREATE_SITE)

    fun frontierLogin(frontierInfo: FrontierUserAndCharacterInfo): UserEntity {
        val user = getOrCreateHackerUser(frontierInfo)

        updateHackerWithCharacterInfo(user, frontierInfo)
        return user
    }


    fun getOrCreateHackerUser(hackerInfo: FrontierUserAndCharacterInfo): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(hackerInfo.frontierExternalReference)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        if (hackerInfo.isGm) {
            return userEntityService.createUser(hackerInfo.frontierExternalReference, UserType.GM, hackerInfo.frontierExternalReference)
        }

        val name = userEntityService.findFreeUserName(hackerInfo.characterName!!)
        val user = userEntityService.createUser(name, UserType.HACKER, hackerInfo.frontierExternalReference)
        hackerEntityService.createHacker(user, HackerIcon.FROG, hackerInfo.characterName, emptyList<Skill>())

        return user
    }

    fun updateHackerWithCharacterInfo(user: UserEntity, frontierInfo: FrontierUserAndCharacterInfo) {
        if (frontierInfo.isGm) return

        val hacker = hackerEntityService.findForUser(user)
        val skills = determineFrontierCharacterSkills(frontierInfo)

        val updatedHacker = hacker.copy(
            characterName = frontierInfo.characterName!!,
            skills = skills,
        )
        hackerEntityService.save(updatedHacker)
    }

    private fun determineFrontierCharacterSkills(frontierInfo: FrontierUserAndCharacterInfo): List<Skill> {
        val v3Skills = orthankService.getPlayerSkills(frontierInfo.characterId!!)
        val hackerLevel = v3Skills.hacker

        if ( hackerLevel == 0) {
            return emptyList() // not a hacker, does not get skills
        }
        if (hackerLevel == 1 || hackerLevel == 2) {
            return LEVEL_1_SKILLS
        }
        return LEVEL_3_SKILLS
    }

}
