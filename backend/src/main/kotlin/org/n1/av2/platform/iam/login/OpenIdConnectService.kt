package org.n1.av2.platform.iam.login

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.contains
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.util.HttpClient
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import java.util.stream.Collectors

class OpenIdConnectUserInfos(
    val externalId: String,
    val userName: String,
    val isGm: Boolean,
    val characterName: String,
    val skills: List<SkillType>
)

@JsonIgnoreProperties(ignoreUnknown = true)
class OpenIdConnectTokens(
    @param:JsonProperty("access_token")
    val accessToken: String,
    @param:JsonProperty("refresh_token")
    val refreshToken: String,
)

@Service
class OpenIdConnectService(
    private val configService: ConfigService,
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
    private val skillService: SkillService,
) {
    private val logger = mu.KotlinLogging.logger {}
    private val decoder = Base64.getUrlDecoder()
    private val objectMapper = jacksonObjectMapper()

    fun getLoginUrl(redirectUri: String): String {
        val baseUrl = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL);
        if (baseUrl.isBlank()) { //fallback to default login method if no URL has been set
            return "/login"
        }

        val clientId = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID);
        return "${baseUrl}/auth?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}"
    }

    fun login(code: String, redirectUri: String): UserEntity? {
        val baseUrl = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL)
        val clientId = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID)

        val oidcTokens = getToken(baseUrl, clientId, code, redirectUri)
        closeSession(baseUrl, clientId, oidcTokens.refreshToken)

        val userInfos = getInfosFromToken(oidcTokens.accessToken)
        val user = getOrCreateHackerUser(userInfos)

        updateHackerWithCharacterInfo(user, userInfos)
        return user
    }

    private fun getToken(baseUrl: String, clientId: String, code: String, redirectUri: String): OpenIdConnectTokens {

        val encodedUrl = UriComponentsBuilder.newInstance()
            .queryParam("grant_type", "authorization_code")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("code", code)
            .build(true)
            .toUriString().replaceFirst("?", "")

        val result = HttpClient().post(
            "${baseUrl}/token",
            encodedUrl,
            mapOf("content-type" to "application/x-www-form-urlencoded")
        )
        val tokens = objectMapper.readValue(result, OpenIdConnectTokens::class.java)
        return tokens
    }

    /* terminating OIDC session after login to force re-login on new call of the login api*/
    private fun closeSession(baseUrl: String, clientId: String, refreshToken: String) {
        val encodedUrl = UriComponentsBuilder.newInstance()
            .queryParam("client_id", clientId)
            .queryParam("refresh_token", refreshToken)
            .build(true)
            .toUriString().replaceFirst("?", "")

        try {
            HttpClient().post(
                "${baseUrl}/logout",
                encodedUrl,
                mapOf("content-type" to "application/x-www-form-urlencoded")
            )
        } catch (e: Exception) {
            logger.error("Error when closing session", e)
        }
    }


    private fun getOrCreateHackerUser(hackerInfo: OpenIdConnectUserInfos): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(hackerInfo.externalId)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        if (hackerInfo.isGm) {
            return userEntityService.createUser(hackerInfo.userName, UserType.GM, hackerInfo.externalId)
        }

        val name = userEntityService.findFreeUserName(hackerInfo.characterName)
        val user = userEntityService.createUser(name, UserType.HACKER, hackerInfo.externalId)
        hackerEntityService.createHacker(user, HackerIcon.FROG, hackerInfo.characterName)

        return user
    }


    private fun updateHackerWithCharacterInfo(user: UserEntity, openIdConnectUserInfos: OpenIdConnectUserInfos) {
        if (openIdConnectUserInfos.isGm) return

        val hacker = hackerEntityService.findForUser(user)
        val updatedHacker = hacker.copy(
            characterName = openIdConnectUserInfos.characterName,
        )
        hackerEntityService.save(updatedHacker)

        skillService.addSkillsForUser(user, openIdConnectUserInfos.skills)
    }

    private fun getInfosFromToken(jwt: String): OpenIdConnectUserInfos {
        val objectMapper = ObjectMapper()

        val chunks = jwt.split(".")
        val payload = String(decoder.decode(chunks[1]))
        val rootNode = objectMapper.readTree(payload)

        val id = getStringField(rootNode, "sub")
        val characterName = getStringField(rootNode, "character_name")
        val userName = getStringField(rootNode, "preferred_username")
        val isGm = hasRole(rootNode, "GM")
        val skills = mapSkills(rootNode)

        return OpenIdConnectUserInfos(id, userName, isGm, characterName, skills)
    }

    private fun getStringField(node: JsonNode, fieldName: String): String {
        val field = node.get(fieldName) ?: return ""
        return field.asText()
    }

    private fun hasRole(tokenPayload: JsonNode, role: String): Boolean {
        val realmAccess = tokenPayload.get("realm_access") ?: return false
        val roles = realmAccess.get("roles") ?: return false
        return roles.contains(role)
    }

    private fun mapSkills(tokenPayload: JsonNode): List<SkillType> {
        val tokenSkills = tokenPayload.get("skills") ?: return emptyList()
        return SkillType.entries.stream()
            .filter { skillName -> tokenSkills.contains(skillName.name) }
            .collect(Collectors.toList())
    }
}



