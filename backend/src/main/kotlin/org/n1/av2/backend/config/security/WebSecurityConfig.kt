package org.n1.av2.backend.config.security

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
                .authorizeRequests()
                .antMatchers("/**/*.{js,html,css,png,jpg,ico}").permitAll()
                .antMatchers("/", "/health",
                        "/edit/**", "/api/**",
                        "/about", "/error", "/loginSubmit",
                        "/notChrome", "/login/*", "/manual/**", "/keepAlive", "/signUp").permitAll()
                .antMatchers("/attack_vector_websocket").permitAll() // .hasAnyRole(ROLE_USER.authority.toString())
                .antMatchers("/hacker/**").permitAll()
                .antMatchers("/gm/**").permitAll()
//
            .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
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