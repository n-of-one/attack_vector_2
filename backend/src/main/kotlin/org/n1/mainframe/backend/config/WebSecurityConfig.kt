package org.n1.mainframe.backend.config

import org.n1.mainframe.backend.model.UserRole
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * Security config for the application
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .antMatchers("/", "/health",
                        "/edit/**", "/api/**", "/ws", "/js/**", "/css/**", "/img/**",
                        "/about", "/resources/**", "/error", "/favicon.ico", "/loginSubmit",
                        "/notChrome", "/login/*", "/manual/**", "/keepAlive", "/signUp", "/ice/magiceye-generator").permitAll()
                .antMatchers("/me/**").hasAnyRole(UserRole.HACKER, UserRole.GM, UserRole.ADMIN)
                .antMatchers("/gm/**").hasRole(UserRole.GM)
                .antMatchers("/print/**").hasAnyRole(UserRole.HACKER, UserRole.GM)
                .antMatchers("/admin/**").hasRole(UserRole.ADMIN)
                .antMatchers("/**").hasRole(UserRole.HACKER)
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .and()
                .csrf()
                .disable()
    }
    //
    //    class MyAuthenticationProvider implements AuthenticationProvider {
    //        @Override
    //        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    //            return null;
    //        }
    //
    //        @Override
    //        public boolean supports(Class<?> aClass) {
    //            return true;
    //        }
    //    }

    //    @Autowired
    //    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    //        auth
    //                .inMemoryAuthentication()
    //                .withUser("user").password("user").roles("gm");
    //    }

    //
    //    @Bean
    //    public MyAuthenticationProvider configureAuthentication(){
    //        return new MyAuthenticationProvider();
    //    }
}