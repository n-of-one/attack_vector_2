package org.n1.av2.backend.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.websocket.ConnectionUtil
import org.n1.av2.backend.config.websocket.ICE_ENDPOINT
import org.n1.av2.backend.config.websocket.MAIN_ENDPOINT
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.iam.ConnectionType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val userEntityService: UserEntityService,
    private val currentUserService: CurrentUserService,
    private val connectionUtil: ConnectionUtil,
) : OncePerRequestFilter() {

    private val logger = mu.KotlinLogging.logger {}

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        var authentication: UserPrincipal? = null

        try {
            val jwt = getJwtFromRequest(request)
            if (jwt != null) {
                if (tokenProvider.validateToken(jwt)) {
                    val userName = tokenProvider.getUserNameFromJWT(jwt)
                    val user = userEntityService.getByName(userName)
                    val connectionId = connectionUtil.create()
                    val type = determineType(request.requestURI)
                    authentication = UserPrincipal("", connectionId, user, type)
                    SecurityContextHolder.getContext().authentication = authentication
                    currentUserService.set(user)

                }
            }
        } catch (exception: Exception) {
            logger.error("Could not set user authentication in security context: ", exception)
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            if (authentication != null) {
                currentUserService.remove()
                connectionUtil.recycle(authentication.connectionId)
                SecurityContextHolder.clearContext()
            }
        }
    }

    private fun determineType(path: String): ConnectionType {
        return when (path) {
            MAIN_ENDPOINT -> ConnectionType.WS_GENERAL
            ICE_ENDPOINT -> ConnectionType.WS_ICE
            else -> ConnectionType.WEB_PAGE
        }
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        if (request.requestURI == "/api/health/") {
            return null
        }
        val cookie = request.cookies?.find { it.name == "jwt" } ?: return null
        return cookie.value
    }
}