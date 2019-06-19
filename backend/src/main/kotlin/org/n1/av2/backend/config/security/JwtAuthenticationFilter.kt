package org.n1.av2.backend.config.security

import mu.KLogging
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.user.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationFilter(
        private val tokenProvider: JwtTokenProvider,
        private val userService: UserService,
        private val currentUserService: CurrentUserService
): OncePerRequestFilter() {

    companion object : KLogging()

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val jwt = getJwtFromRequest(request)

            if (jwt != null ) {
                if (tokenProvider.validateToken(jwt)) {
                    val userName = tokenProvider.getUserNameFromJWT(jwt)

                    val user = userService.getByName(userName)
                    val authentication = UserPrincipal(user)
                    SecurityContextHolder.getContext().authentication = authentication
                    currentUserService.set(authentication)
                }
            }
        } catch (exception: Exception) {
            logger.error("Could not set user authentication in security context", exception)
        }

        filterChain.doFilter(request, response)

        currentUserService.remove()
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        if (request.requestURI == "/api/health/") {
            return null
        }
        val cookie = request.cookies?.find { it.name == "jwt" } ?: return null
        return cookie.value
    }
}