package org.n1.av2.backend.config.security

import org.n1.av2.backend.entity.user.ROLE_GM
import org.n1.av2.backend.entity.user.ROLE_SITE_MANAGER
import org.n1.av2.backend.entity.user.ROLE_USER
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * Security config for the application
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig(val jwtAuthenticationFilter: JwtAuthenticationFilter) {


    @Bean
    @Throws(Exception::class)
    fun configure(http: HttpSecurity): SecurityFilterChain {

        http
//                .exceptionHandling()
//                .authenticationEntryPoint(unauthorizedHandler)
//            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()

            .requestMatchers("/", "/css/**", "/img/**", "/resources/**", "/index.html", "/static/**",
                "/favicon.ico", "/asset-manifest.json").permitAll()

            .requestMatchers("/localLogout", "/loggedOut", "/login", "/sso",).permitAll()
            .requestMatchers("/about").permitAll()

            .requestMatchers("/ws_hacker").hasAuthority(ROLE_USER.authority)
            .requestMatchers("/ws_networked_app").hasAuthority(ROLE_USER.authority)
            .requestMatchers("/ws_unrestricted").permitAll()

            .requestMatchers("/login/*", "/api/login", "/signUp", "/logout").permitAll()
            .requestMatchers("/api/**").hasAuthority(ROLE_USER.authority)

            // HTML routes
            .requestMatchers("/hacker/**").permitAll()
            .requestMatchers("/ice/**").permitAll()
            .requestMatchers("/edit/**").hasAuthority(ROLE_SITE_MANAGER.authority)
            .requestMatchers("/gm/**").hasAuthority(ROLE_GM.authority)
            .requestMatchers("/manual/**").permitAll()
            .and()
            .csrf()
            .disable()
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

//    @Throws(Exception::class)
//    public override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder?) {
//        authenticationManagerBuilder!!
//                .userDetailsService(customUserDetailsService)
//                .passwordEncoder(passwordEncoder())
//    }

//    @Bean(BeanIds.AUTHENTICATION_MANAGER)
//    @Throws(Exception::class)
//    override fun authenticationManagerBean(): AuthenticationManager {
//        return super.authenticationManagerBean()
//    }
//
//    @Bean
//    fun passwordEncoder(): PasswordEncoder {
//        return BCryptPasswordEncoder()
//    }


}

@Configuration
class WebConfiguration : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins("http://localhost.eosfrontier.space").allowedMethods("*")
    }
}