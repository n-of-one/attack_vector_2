package org.n1.mainframe.backend.config

import org.n1.mainframe.backend.config.security.JwtAuthenticationFilter
import org.n1.mainframe.backend.model.iam.UserRole
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


/**
 * Security config for the application
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig(val jwtAuthenticationFilter: JwtAuthenticationFilter)
    : WebSecurityConfigurerAdapter() {




    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
//                .exceptionHandling()
//                .authenticationEntryPoint(unauthorizedHandler)
//            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers("/", "/health",
                        "/edit/**", "/api/**", "/js/**", "/css/**", "/img/**",
                        "/about", "/resources/**", "/error", "/favicon.ico", "/loginSubmit",
                        "/notChrome", "/login/*", "/manual/**", "/keepAlive", "/signUp", "/ice/magiceye-generator").permitAll()
                .antMatchers("/attack_vector_websocket").permitAll() // .hasAnyRole(ROLE_USER.authority.toString())
                .antMatchers("/me/**").hasAnyRole(UserRole.HACKER, UserRole.GM, UserRole.ADMIN)
                .antMatchers("/gm/**").hasRole(UserRole.GM)
                .antMatchers("/print/**").hasAnyRole(UserRole.HACKER, UserRole.GM)
                .antMatchers("/admin/**").hasRole(UserRole.ADMIN)
                .antMatchers("/**").hasRole(UserRole.HACKER)
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