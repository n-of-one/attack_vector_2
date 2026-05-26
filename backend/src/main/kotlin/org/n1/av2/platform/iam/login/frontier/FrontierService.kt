package org.n1.av2.platform.iam.login.frontier

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.EXTERNAL_USER_MANAGER
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.script.ram.RamService
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
    private val skillService: SkillService,
    private val scriptAccesService: ScriptAccessService,
    private val currentUserService: CurrentUserService,
    private val ramService: RamService,
) {

    fun frontierLogin(frontierInfo: FrontierUserAndCharacterInfo): UserEntity {
        try {
            currentUserService.set(EXTERNAL_USER_MANAGER)
            val user = getOrCreateHackerUser(frontierInfo)

            updateHackerWithCharacterInfo(user, frontierInfo)
            return user
        }
        finally {
            currentUserService.remove()
        }
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
        hackerEntityService.createHacker(user, HackerIcon.FROG, hackerInfo.characterName)

        return user
    }

    fun updateHackerWithCharacterInfo(user: UserEntity, frontierInfo: FrontierUserAndCharacterInfo) {
        if (frontierInfo.isGm) return

        updateCharacterName(user, frontierInfo)
        val v3Skills = orthankService.getPlayerSkills(frontierInfo.characterId!!)

        updateHackerSkillsAndRam(user, v3Skills)
        updateHackerScripts(user, frontierInfo, v3Skills)
    }

    private fun updateCharacterName(user: UserEntity, frontierInfo: FrontierUserAndCharacterInfo) {
        val hacker = hackerEntityService.findForUser(user)
        val updatedHacker = hacker.copy(
            characterName = frontierInfo.characterName!!,
        )
        hackerEntityService.save(updatedHacker)
    }

    private fun updateHackerSkillsAndRam(user: UserEntity, v3Skills:FrontierV3HackingSkills) {
        val translator = FrontierSkillTranslator(v3Skills)
        val skillTypes = translator.translateSkills()
        skillService.updateSkillsForUser(user, skillTypes)

        val ramSize = skillTypes[SkillType.SCRIPT_RAM]?.toInt() ?: 0
        ramService.updateRam(user.id, ramSize)
    }

    private fun updateHackerScripts(user: UserEntity, frontierInfo: FrontierUserAndCharacterInfo, v3Skills:FrontierV3HackingSkills) {
        val translator = FrontierScriptsAccessTranslator(frontierInfo, v3Skills)
        val accessInfo = translator.determineAccessInfo()

        scriptAccesService.updateScriptAccessForUser(user.id, accessInfo)
    }

}
