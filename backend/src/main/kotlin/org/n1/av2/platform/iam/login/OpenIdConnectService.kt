package org.n1.av2.platform.iam.login

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.URLEncoder


class OpenIdConnectUserInfos(
    val externalId: String,
    val userName: String,
    val type: UserType,
    val characterName: String,
    val skills: List<SkillType>,
    val useTemplatedSkills: Boolean
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
    private val httpClient: HttpClient,
    private val jwtDecoderProvider: OpenIdConnectJwtDecoderProvider,
    private val jsonMapper: JsonMapper,
) {
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

        val response = httpClient.post(
            "${baseUrl}/token",
            encodedUrl,
            mapOf(
                "Content-Type" to "application/x-www-form-urlencoded",
                "Host" to URI(baseUrl).host
            )
        )
        if (!response.isOk() || response.body.isEmpty()) {
            error("Can't retrieve oidc tokens")
        }
        val body = jsonMapper.readTree(response.body)
        if (body.has("error")) {
            error("Can't retrieve oidc tokens: ${body.get("error").asString()}")
        }
        return jsonMapper.treeToValue(body, OpenIdConnectTokens::class.java)
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

        httpClient.post(
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

        val name = userEntityService.findFreeUserName(userInfos.userName)
        return userEntityService.createUser(name, userInfos.type, userInfos.externalId)
    }


    private fun createOrUpdateHacker(user: UserEntity, openIdConnectUserInfos: OpenIdConnectUserInfos) {
        if (UserType.GM == openIdConnectUserInfos.type) return

        val hacker = hackerEntityService.findForUserOrNull(user)
        if (hacker == null) {
            hackerEntityService.createHacker(user, HackerIcon.FROG, openIdConnectUserInfos.characterName)
            hackerEntityService.findForUser(user)
        } else {
            val updatedHacker = hacker.copy(
                characterName = openIdConnectUserInfos.characterName,
            )
            hackerEntityService.save(updatedHacker)
        }

        if (openIdConnectUserInfos.useTemplatedSkills) {
            skillService.createDefaultSkills(user.id)
        }
        skillService.addSkillsForUser(user, openIdConnectUserInfos.skills)
    }


    private fun getInfosFromToken(accessToken: String, clientId: String): OpenIdConnectUserInfos {
        val jwt = jwtDecoderProvider.decoder().decode(accessToken) // throws JwtException if invalid

        val id = jwt.getClaimAsString("sub") ?: ""
        val characterName = jwt.getClaimAsString("character_name") ?: ""
        val userName = jwt.getClaimAsString("preferred_username") ?: ""
        val type = getUserType(jwt, clientId)
        val skills = mapSkills(jwt, clientId)
        val useTemplatedSkills = hasRole(jwt, clientId, "USE_TEMPLATE_SKILLS")

        return OpenIdConnectUserInfos(id, userName, type, characterName, skills, useTemplatedSkills)
    }

    private fun getUserType(jwt: Jwt, clientId: String): UserType {
        if (hasRole(jwt, clientId, "GM")) {
            return UserType.GM
        }
        return UserType.HACKER
    }

    private fun hasRole(jwt: Jwt, clientId: String, roleName: String): Boolean {
        if (roleName.isBlank()) {
            return false
        }
        return roleName in getRolesFromToken(jwt, clientId)
    }

    private fun getRolesFromToken(jwt: Jwt, clientId: String): Set<String> {
        return getRealmRoles(jwt) + getClientRoles(jwt, clientId)
    }

    private fun getRealmRoles(jwt: Jwt): Set<String> {
        val realmAccess = jwt.getClaimAsMap("realm_access") ?: return emptySet()
        return extractRoles(realmAccess)
    }

    private fun getClientRoles(jwt: Jwt, clientId: String): Set<String> {
        val resourceAccess = jwt.getClaimAsMap("resource_access") ?: return emptySet()
        val client = resourceAccess[clientId] as? Map<*, *> ?: return emptySet()
        return extractRoles(client)
    }

    private fun extractRoles(access: Map<*, *>): Set<String> {
        val roles = access["roles"] as? Collection<*> ?: return emptySet()
        return roles.filterIsInstance<String>().toSet()
    }

    private fun mapSkills(jwt: Jwt, clientId: String): List<SkillType> {
        if (hasRole(jwt, clientId, "SITE_ADMIN")) {
            return listOf(SEARCH_SITE, SCAN, CREATE_SITE)
        }

        if (hasRole(jwt, clientId, "HACKER")) {
            return listOf(SEARCH_SITE, SCAN)
        }

        return listOf()
    }
}



