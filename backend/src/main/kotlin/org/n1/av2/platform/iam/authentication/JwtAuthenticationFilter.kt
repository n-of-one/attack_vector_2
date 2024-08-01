package org.n1.av2.platform.iam.authentication

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.platform.connection.ConnectionIdService
import org.n1.av2.platform.connection.connectionTypeFromPath
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val userEntityService: UserEntityService,
    private val currentUserService: CurrentUserService,
    private val connectionIdService: ConnectionIdService,
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        if (dontNeedAuthentication(request)) {
            filterChain.doFilter(request, response)
            return
        }

        var authentication: UserPrincipal? = null

        try {
            val jwt = getJwtFromRequest(request)
            if (jwt != null) {
                authentication = parseJwt(jwt, request)
            }
        } catch (exception: Exception) {
            logger.error("Could not set user authentication in security context: ", exception)
        }

        if (authentication == null) {
            // Need to set authentication to make AccessDeniedHandler work
            authentication = UserPrincipal.notLoggedIn()
            SecurityContextHolder.getContext().authentication = authentication
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            currentUserService.remove()
            connectionIdService.recycle(authentication.connectionId)
            SecurityContextHolder.clearContext()
        }
    }

    private fun parseJwt(jwt: String, request: HttpServletRequest): UserPrincipal? {
        if (!tokenProvider.validateToken(jwt)) {
            return null
        }
        val userId = tokenProvider.getUserIdFromJWT(jwt)
        val user = userEntityService.getById(userId)
        val connectionId = connectionIdService.create()

        val type = connectionTypeFromPath(request.requestURI)
        val authentication = UserPrincipal("", connectionId, user, type)
        SecurityContextHolder.getContext().authentication = authentication
        currentUserService.set(user)

        return authentication
    }

    private fun dontNeedAuthentication(request: HttpServletRequest): Boolean {
        // don't parse JWT for static resources
        request.requestURI.let {
            return (
                    it.startsWith("/resources") ||
                    it.startsWith("/img") ||
                    it.startsWith("/css") ||
                    it.startsWith("/static")
                    )
        }
    }


    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val cookie = request.cookies?.find { it.name == "jwt" } ?: return null
        return cookie.value
    }
}
