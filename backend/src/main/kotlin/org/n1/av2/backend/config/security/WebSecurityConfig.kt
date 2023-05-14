package org.n1.av2.backend.config.security

import org.n1.av2.backend.entity.user.ROLE_HACKER
import org.n1.av2.backend.entity.user.ROLE_USER
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


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

            .requestMatchers("/av_ws").hasAuthority(ROLE_USER.authority.toString())
            .requestMatchers("/ice_ws").hasAuthority(ROLE_USER.authority.toString())

            .requestMatchers("/login/*", "/api/login", "/signUp").permitAll()
            .requestMatchers("/api/**").hasAuthority(ROLE_USER.authority.toString())

            // HTML routes
            .requestMatchers("/").permitAll()
            .requestMatchers("/ice/**").hasAuthority(ROLE_HACKER.authority.toString())
            .requestMatchers("/hacker/**").hasAuthority(ROLE_USER.authority.toString())
            .requestMatchers("/gm/**").hasAuthority(ROLE_USER.authority.toString())
            .requestMatchers("/about").permitAll()
            .requestMatchers("/manual/**").permitAll()
//
            .and()
            .logout()
            .permitAll()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
//
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