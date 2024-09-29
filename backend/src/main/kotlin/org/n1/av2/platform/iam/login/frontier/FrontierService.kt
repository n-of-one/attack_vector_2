package org.n1.av2.platform.iam.login.frontier

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.n1.av2.larp.frontier.LolaService
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.util.HttpClient
import org.springframework.stereotype.Service

const val FRONTIER_GM_GROUP = "30"

class FrontierHackerInfo(
    val id: String,
    val isGm: Boolean,
    val characterName: String? = null,
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
    private val configService: ConfigService,
    private val lolaService: LolaService,
) {
    private val httpClient = HttpClient()
    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    class FrontierUserInfo(
        val id: String,
        val gm: Boolean
    )

    class CharacterInfo(
        @JsonProperty("character_name") var characterName: String,
        @JsonProperty("characterID") var characterId: String,
    ) {
        constructor() : this("", "")
    }

    fun getFrontierHackerInfo(cookies: Array<Cookie>): FrontierHackerInfo? {
        val userInfo = getUserInfo(cookies)
        if (userInfo.id == "0") {
            return null
        }
        if (userInfo.gm) {
            return FrontierHackerInfo(
                id = "frontier-gm:${userInfo.id}",
                isGm = true
            )
        }
        val playerInfo = getPlayerInfo(userInfo.id)

        return FrontierHackerInfo(
            id = "frontier-player:${playerInfo.characterId}",
            isGm = false,
            characterName = playerInfo.characterName,
        )
    }

    private fun getUserInfo(cookies: Array<Cookie>): FrontierUserInfo {
        class IdAndGroupResponse(var id: String, var groups: List<String>?) {
            constructor() : this("", emptyList())
        }

        val idAndGroupsText = httpClient.get("https://ic.eosfrontier.space/assets/idandgroups.php", emptyMap(), cookies)
        val idAndGroups = objectMapper.readValue(idAndGroupsText, IdAndGroupResponse::class.java)
        val gm = idAndGroups.groups?.contains(FRONTIER_GM_GROUP) ?: false

        return FrontierUserInfo(idAndGroups.id, gm)
    }

    private fun orthankToken(): String {
        return configService.get(ConfigItem.FRONTIER_ORTHANK_TOKEN)
    }
    private fun getPlayerInfo(accountId: String): CharacterInfo {
        val headers = mapOf("accountID" to accountId, "token" to orthankToken())
        val responseText = httpClient.get("https://api.eosfrontier.space/orthanc/v2/chars_player/", headers)
        println(responseText)
        val playerInfo = objectMapper.readValue(responseText, CharacterInfo::class.java)

        return playerInfo
    }

    class FrontierV3HackingSkills(
        val hacker: Int = 0,
        val architect: Int = 0,
        val elite: Int = 0
        )

    // Using: https://github.com/eosfrontier/orthanc/tree/master/v2/chars_all
    private fun getPlayerSkills(characterId: String): FrontierV3HackingSkills {
        class SkillInfo(var name: String, var level: Int) {
            constructor() : this("", 0)
        }

        val headers = mapOf("id" to characterId, "token" to orthankToken())
        val responseText = httpClient.get("https://api.eosfrontier.space/orthanc/v2/chars_player/skills/", headers)
        println(responseText)
        val skillsInfo: List<SkillInfo> = objectMapper.readValue(responseText, object : TypeReference<List<SkillInfo>>() {})

        val hackerSkill = FrontierV3HackingSkills(
            hacker = skillsInfo.find { it.name == "it" }?.level ?: 0,
            architect = skillsInfo.find { it.name == "itarchi" }?.level ?: 0,
            elite = skillsInfo.find { it.name == "itelite" }?.level ?: 0
        )
        return hackerSkill
    }

    fun createLolaUser() {
        lolaService.createLolaUser()
    }

    fun processShare(runId: String, user: UserEntity):Boolean {
        if (user.name.uppercase() == "LOLA") {
            lolaService.share(user, runId)
            return true
        }
        return false
    }
}
