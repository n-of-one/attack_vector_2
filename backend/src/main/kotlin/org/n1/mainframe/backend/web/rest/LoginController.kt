package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.config.security.JwtTokenProvider
import org.n1.mainframe.backend.model.user.UserAuthentication
import org.n1.mainframe.backend.service.user.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/api")
class LoginController(
                      private val userService: UserService,
//                      private val authenticationManager: AuthenticationManager,
                      private val jwtTokenProvider: JwtTokenProvider) {

    enum class LoginRole { ADMIN, GM, SUPER_USER, USER }
    data class LoginInput(val name: String = "", val password: String = "")
    data class LoginResponse(val success:Boolean, val role: LoginRole?, val message: String? = null)

//    @PostMapping("/login")
//    fun login(@RequestBody input: LoginInput): LoginResponse {
//        try {
//            val user = userService.login(input.name, input.password)
//            val authentication = UserAuthentication(user)
//            SecurityContextHolder.getContext().authentication = authentication
//            return LoginResponse("token-3923-32424-23423", LoginRole.ADMIN )
//        }
//        catch (exception: UsernameNotFoundException) {
//            return LoginResponse(null, null, exception.message)
//        }
//    }

    @PostMapping("/login")
    fun login(@RequestBody input: LoginInput, response: HttpServletResponse): LoginResponse {

//        val loginDetails =  UsernamePasswordAuthenticationToken( input.name,  input.password )
//        val authentication = authenticationManager.authenticate( loginDetails )

        try {
            val user = userService.login(input.name, input.password)
            val authentication = UserAuthentication(user)

            SecurityContextHolder.getContext().authentication = authentication

            val jwt = jwtTokenProvider.generateJwt(user)

            val cookie = Cookie("jwt", jwt)
            cookie.path = "/"
            cookie.maxAge = 60 * 5
            response.addCookie(cookie)

            return LoginResponse(true, LoginRole.ADMIN )
        }
        catch (exception: UsernameNotFoundException) {
            return LoginResponse(false, null, "UseName or password invalid")
        }
    }

}