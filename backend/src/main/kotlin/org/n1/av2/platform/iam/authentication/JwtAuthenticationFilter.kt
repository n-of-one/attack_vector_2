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
        var errorMessage: String = ""

        try {
            val jwt = getJwtFromRequest(request)
            if (jwt != null) {
                val (authResult, authErrorMessage) = parseJwt(jwt, request)
                authentication = authResult
                errorMessage = authErrorMessage
            }
            else {
                errorMessage = "No JWT token supplied."
            }
        } catch (exception: Exception) {
            logger.error("Could not set user authentication in security context: ", exception)
            errorMessage = "Could not set user authentication in security context."
        }

        if (authentication == null) {
            if (request.requestURI.startsWith("/api")) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.writer.write(errorMessage)
                response.writer.flush()
                return
            }
            logger.warn("Authentication failed: $errorMessage")

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

    private fun parseJwt(jwt: String, request: HttpServletRequest): Pair<UserPrincipal?, String> {
        val (validToken, errorMessage) = tokenProvider.validateToken(jwt)
        if (!validToken) {
            return null to errorMessage
        }
        val userId = tokenProvider.getUserIdFromJWT(jwt)
        val user = userEntityService.getById(userId)
        val connectionId = connectionIdService.create()

        val type = connectionTypeFromPath(request.requestURI)
        val authentication = UserPrincipal("", connectionId, user, type)
        SecurityContextHolder.getContext().authentication = authentication
        currentUserService.set(user)

        return authentication to ""
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
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }
        val cookie = request.cookies?.find { it.name == "jwt" } ?: return null
        return cookie.value
    }
}
