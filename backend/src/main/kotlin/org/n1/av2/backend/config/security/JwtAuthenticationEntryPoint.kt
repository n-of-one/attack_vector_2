package org.n1.av2.backend.config.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException


//@Component
//class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
//
//    private val logger = mu.KotlinLogging.logger {}
//
//    @Throws(IOException::class, ServletException::class)
//    override fun commence(httpServletRequest: HttpServletRequest,
//                          httpServletResponse: HttpServletResponse,
//                          exception: AuthenticationException) {
//        logger.error("Responding with unauthorized error. Message - {}", exception.message)
//        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
//    }
//}