package org.n1.av2.backend.config.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.entity.user.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private val OPEN_PATHS = listOf(
    "/", "/index.html",
    "/css/**", "/img/**", "/resources/**", "/static/**", "/favicon.ico", "/manifest.json", "/asset-manifest.json",
    "/about", "/privacy", "/website/**",
    "/loggedOut", "/login", "/adminLogin", "/logout", "/localLogout", "login-frontier",

    "/o/**", // for widgets and other apps that don't require any authentication
    "/openapi/**", // open api calls

    "/ws_unrestricted", // websocket path for widgets and other apps that don't require any authentication

    // The following endspoints functionally have .hasAuthority(ROLE_USER.authority)
    // But the client cannot distinguish between a rejected connection and a server that is down. So instead, we do the authentication check in
    // the handshake handler. See: StompConfig.kt AuthenticatedHandshakeHandler. This allows us to tell the client to redirect to the login page instead.
    "/ws_hacker", "/ws_networked_app"
)

private val HACKER_PATHS = listOf(
    "/hacker/**",
    "/x/**", // for widgets and other apps that require hacker authentication
)

private val GM_PATHS = listOf("/gm/**", "/edit/**")
private val USER_PATHS = listOf("/api/**")

@Configuration
@EnableWebSecurity
class WebSecurityConfig(val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    @Throws(Exception::class)
    fun configure(http: HttpSecurity): SecurityFilterChain {

        http.authorizeHttpRequests { requests ->
            OPEN_PATHS.forEach { path -> requests.requestMatchers(path).permitAll() }
            HACKER_PATHS.forEach { path -> requests.requestMatchers(path).hasAuthority(ROLE_HACKER.authority) }
            GM_PATHS.forEach { path -> requests.requestMatchers(path).hasAuthority(ROLE_GM.authority) }
            USER_PATHS.forEach { path -> requests.requestMatchers(path).hasAuthority(ROLE_USER.authority) }
        }
        http.csrf { it.disable() }
        http.headers { headers -> headers.frameOptions { it.disable() } } // for IMD.html
        http.exceptionHandling { customizer -> customizer.accessDeniedHandler(customAccessDeniedHandler()) }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun customAccessDeniedHandler(): AccessDeniedHandler {
        return AccessDeniedHandler { request: HttpServletRequest, response: HttpServletResponse, _: AccessDeniedException? ->
            val path = request.requestURI
            val redirectUrl = request.contextPath + "/login?next=$path"
            response.sendRedirect(redirectUrl)
        }
    }
}

@Configuration
class WebConfiguration : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        // disable CORS for local development
        registry.addMapping("/**").allowedOrigins("http://localhost", "http://localhost:3000", "https://av.eosfrontier.space", "https://eosfrontier.space")
            .allowedMethods("*").allowCredentials(true)
    }
}
