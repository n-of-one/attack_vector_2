package org.n1.av2.backend.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.websocket.ConnectionUtil
import org.n1.av2.backend.config.websocket.connectionTypeFromPath
import org.n1.av2.backend.entity.user.UserEntityService
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

        if (dontNeedAuthentication(request)) {
            logger.info("Don't need authentication for ${request.requestURI}")
            filterChain.doFilter(request, response)
            return
        }

        var authentication: UserPrincipal? = null

        try {
            val jwt = getJwtFromRequest(request)
            if (jwt == null) {
                logger.info("No jwt found in request")
            } else {
                authentication = parseJwt(jwt, request)
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

    private fun parseJwt(jwt: String, request: HttpServletRequest): UserPrincipal? {
        if (!tokenProvider.validateToken(jwt)) {
            logger.info("JWT is not valid: ${jwt}")
            return null
        }
        val userId = tokenProvider.getUserIdFromJWT(jwt)
        val user = userEntityService.getById(userId)
        val connectionId = connectionUtil.create()
        val type = connectionTypeFromPath(request.requestURI)
        val authentication = UserPrincipal("", connectionId, user, type)
        SecurityContextHolder.getContext().authentication = authentication
        currentUserService.set(user)

        val authorities = authentication.authorities.map { it.authority }.joinToString(", ")
        logger.info("Valid authentication for ${user.name}. authorities: ${authorities} ")
        return authentication
    }

    private fun dontNeedAuthentication(request: HttpServletRequest): Boolean {
        request.requestURI.let {
            return (it.startsWith("/api/health/") ||
                    it.startsWith("/logout") ||
                    it.startsWith("/login") ||
                    it.startsWith("/sso") ||
                    it.startsWith("/css") ||
                    it.startsWith("/img") ||
                    it.startsWith("/static") ||
                    it.startsWith("/resources") ||
                    it.startsWith("/favicon.ico")
                    )
        }
    }


    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val cookie = request.cookies?.find { it.name == "jwt" } ?: return null
        return cookie.value
    }
}