package org.n1.mainframe.backend.config

import org.n1.mainframe.backend.service.user.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder



@Configuration
class SecurityConfig(
//        val userDetailsService: UserService,
//        val passwordEncoder: PasswordEncoder
) {

//    fun configureGlobal(auth: AuthenticationManagerBuilder) {
//        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder)
//    }

    //    @Bean
    //    fun createUserDetailsService(): UserDetailsService {
    //
    //    }

    //
    //    @Bean
    //    public MyAuthenticationProvider configureAuthentication(){
    //        return new MyAuthenticationProvider();
    //    }

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

}