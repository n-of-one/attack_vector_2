package org.n1.av2.backend.config

import mu.KLogging
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    @Throws(IOException::class, ServletException::class)
    override fun commence(httpServletRequest: HttpServletRequest,
                          httpServletResponse: HttpServletResponse,
                          exception: AuthenticationException) {
        logger.error("Responding with unauthorized error. Message - {}", exception.message)
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
    }

    companion object : KLogging()
}