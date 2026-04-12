package org.n1.av2.platform.iam.login

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.hacker.skill.SkillType.*
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.util.HttpClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.util.*
import java.util.stream.StreamSupport


class OpenIdConnectUserInfos(
    val externalId: String,
    val userName: String,
    val type: UserType,
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
    private val decoder = Base64.getUrlDecoder()
    private val objectMapper = jacksonObjectMapper()

    fun getLoginUrl(redirectUri: String): String {
        val baseUrl = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL)
        if (baseUrl.isBlank()) { //fallback to default login method if no URL has been set
            return "/login"
        }

        val clientId = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID)
        return "${baseUrl}/auth?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}"
    }

    fun login(code: String, redirectUri: String): UserEntity? {
        val baseUrl = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL)
        val clientId = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID)

        val oidcTokens = getToken(baseUrl, clientId, code, redirectUri)
        closeSession(baseUrl, clientId, oidcTokens.refreshToken)

        val userInfos = getInfosFromToken(oidcTokens.accessToken, clientId)

        val user = getOrCreateUser(userInfos)
        createOrUpdateHacker(user, userInfos)
        return user
    }

    private fun getToken(baseUrl: String, clientId: String, code: String, redirectUri: String): OpenIdConnectTokens {

        val encodedUrl = buildEncodedUrl(
            mapOf(
                "grant_type" to "authorization_code",
                "client_id" to clientId,
                "code" to code,
                "redirect_uri" to redirectUri
            )
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val response = HttpClient().post(
            "${baseUrl}/token",
            encodedUrl,
            mapOf(
                "Content-Type" to "application/x-www-form-urlencoded",
                "Host" to "auth.localhost"
            )
        )
        if (!response.isOk() || response.body.isEmpty() || response.body.contains("error")) {
            error("Can't retrieve oidc tokens")
        }
        return objectMapper.readValue(response.body, OpenIdConnectTokens::class.java)
    }

    private fun buildEncodedUrl(params: Map<String, String>): String {
        val sb = StringBuilder()
        var first = true
        params.forEach {
            if (!first) sb.append("&") else first = false
            sb.append(URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8"))
        }
        return sb.toString()
    }

    /* terminating OIDC session after login to force re-login on new call of the login api*/
    private fun closeSession(baseUrl: String, clientId: String, refreshToken: String) {

        val encodedUrl = buildEncodedUrl(
            mapOf(
                "client_id" to clientId,
                "refresh_token" to refreshToken
            )
        )

        HttpClient().post(
            "${baseUrl}/logout",
            encodedUrl,
            mapOf("content-type" to "application/x-www-form-urlencoded")
        )
    }

    private fun getOrCreateUser(userInfos: OpenIdConnectUserInfos): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(userInfos.externalId)

        if (existingUserEntity != null) {
            if (existingUserEntity.type != userInfos.type) {
                existingUserEntity.type = userInfos.type
                return userEntityService.save(existingUserEntity)
            }
            return existingUserEntity
        }

        val name = userEntityService.findFreeUserName(userInfos.characterName)
        return userEntityService.createUser(name, userInfos.type, userInfos.externalId)
    }


    private fun createOrUpdateHacker(user: UserEntity, openIdConnectUserInfos: OpenIdConnectUserInfos) {
        if (UserType.GM == openIdConnectUserInfos.type) return

        val hacker = hackerEntityService.findForUserOrNull(user)
        if (hacker == null) {
            hackerEntityService.createHacker(user, HackerIcon.FROG, openIdConnectUserInfos.characterName)
            hackerEntityService.findForUser(user);
        } else {
            val updatedHacker = hacker.copy(
                characterName = openIdConnectUserInfos.characterName,
            )
            hackerEntityService.save(updatedHacker)
        }

        skillService.addSkillsForUser(user, openIdConnectUserInfos.skills)
    }


    private fun getInfosFromToken(jwt: String, clientId: String): OpenIdConnectUserInfos {
        val objectMapper = ObjectMapper()

        val chunks = jwt.split(".")
        val payload = String(decoder.decode(chunks[1]))
        val rootNode = objectMapper.readTree(payload)

        val id = getStringField(rootNode, "sub")
        val characterName = getStringField(rootNode, "character_name")
        val userName = getStringField(rootNode, "preferred_username")
        val type = getUserType(rootNode, clientId)
        val skills = mapSkills(rootNode, clientId)

        return OpenIdConnectUserInfos(id, userName, type, characterName, skills)
    }

    private fun getStringField(node: JsonNode, fieldName: String): String {
        val field = node.get(fieldName) ?: return ""
        return field.asText()
    }

    private fun getUserType(tokenPayload: JsonNode, clientId: String): UserType {
        if (hasRole(tokenPayload, clientId, "GM")) {
            return UserType.GM
        }
        return UserType.HACKER
    }

    private fun hasRole(tokenPayload: JsonNode, clientId: String, roleName: String): Boolean {
        if (roleName.isBlank()) {
            return false
        }

        val roles = getRolesFromToken(tokenPayload, clientId)
        return roles.stream()
            .filter(Objects::nonNull)
            .flatMap { StreamSupport.stream(it.spliterator(), false) }
            .filter(Objects::nonNull)
            .anyMatch { role -> roleName == role.asText() }

    }

    private fun getRolesFromToken(tokenPayload: JsonNode, clientId: String): Set<JsonNode> {
        val realmRoles = getRealmRoles(tokenPayload)
        val clientRoles = getClientRoles(tokenPayload, clientId)
        return setOfNotNull(realmRoles, clientRoles)
    }

    private fun getRealmRoles(tokenPayload: JsonNode): JsonNode? {
        val realmAccess = tokenPayload.get("realm_access") ?: return null
        return realmAccess.get("roles") ?: return null
    }

    private fun getClientRoles(tokenPayload: JsonNode, clientId: String): JsonNode? {
        val resourceAccess = tokenPayload.get("resource_access") ?: return null
        val client = resourceAccess.get(clientId) ?: return null
        return client.get("roles") ?: return null
    }

    private fun mapSkills(tokenPayload: JsonNode, clientId: String): List<SkillType> {
        if (hasRole(tokenPayload, clientId, "SITE_ADMIN")) {
            return listOf(SEARCH_SITE, SCAN, CREATE_SITE)
        }

        if (hasRole(tokenPayload, clientId, "HACKER")) {
            return listOf(SEARCH_SITE, SCAN)
        }

        return listOf()
    }
}



