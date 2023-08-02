package org.n1.av2.backend.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.entity.user.HackerSkill
import org.springframework.stereotype.Service

const val FRONTIER_GM_GROUP = "30"

class FrontierHackerInfo(
    val id: String,
    val isGm: Boolean,
    val characterName: String? = null,
    val skills: HackerSkill? = null,
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
    val config: ServerConfig
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
        val playerSkills = getPlayerSkills(playerInfo.characterId)

        return FrontierHackerInfo(
            id = "frontier-player:${playerInfo.characterId}",
            isGm = false,
            characterName = playerInfo.characterName,
            skills = playerSkills,
        )
    }

    private fun getUserInfo(cookies: Array<Cookie>): FrontierUserInfo {
        class IdAndGroupResponse(var id: String, var groups: List<String>?) {
            constructor() : this("", emptyList())
        }

        val idAndGroupsText = httpClient.get("https://ic.eosfrontier.space/assets/idandgroups.php", emptyMap(), cookies)
        println(idAndGroupsText)
        val idAndGroups = objectMapper.readValue(idAndGroupsText, IdAndGroupResponse::class.java)
        val gm = idAndGroups.groups?.contains(FRONTIER_GM_GROUP) ?: false

        return FrontierUserInfo(idAndGroups.id, gm)
    }

    private fun getPlayerInfo(accountId: String): CharacterInfo {
        val headers = mapOf("accountID" to accountId, "token" to config.orthankToken)
        val responseText = httpClient.get("https://api.eosfrontier.space/orthanc/v2/chars_player/", headers)
        println(responseText)
        val playerInfo = objectMapper.readValue(responseText, CharacterInfo::class.java)

        return playerInfo
    }

    // Using: https://github.com/eosfrontier/orthanc/tree/master/v2/chars_all
    private fun getPlayerSkills(characterId: String): HackerSkill {
        class SkillInfo(var name: String, var level: Int) {
            constructor() : this("", 0)
        }

        val headers = mapOf("id" to characterId, "token" to config.orthankToken)
        val responseText = httpClient.get("https://api.eosfrontier.space/orthanc/v2/chars_player/skills/", headers)
        println(responseText)
        val skillsInfo: List<SkillInfo> = objectMapper.readValue(responseText, object : TypeReference<List<SkillInfo>>() {})

        val hackerSkill = HackerSkill(
            hacker = skillsInfo.find { it.name == "it" }?.level ?: 0,
            architect = skillsInfo.find { it.name == "itarchi" }?.level ?: 0,
            elite = skillsInfo.find { it.name == "itelite" }?.level ?: 0
        )
        return hackerSkill
    }

}