package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.model.UserRole.Companion.ADMIN
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/login")
class LoginController {

    enum class LoginRole { ADMIN, GM, SUPER_USER, USER }
    data class LoginInput(val name: String = "", val password: String = "")
    data class LoginResponse(val token:String?, val role: LoginRole?,  val message: String? = null)

    @PostMapping("/")
    fun login(@RequestBody input: LoginInput): LoginResponse {
        return when (input.name) {
            "admin" -> LoginResponse("token:3923-32424-23423", LoginRole.ADMIN )
            "user" -> LoginResponse("token:3923-32424-23423", LoginRole.USER )
            "gm" -> LoginResponse("token:3923-32424-23423", LoginRole.GM )
            "superuser" -> LoginResponse("token:3923-32424-23423", LoginRole.SUPER_USER )
            "" -> LoginResponse(null, null, "No input specified")
            else -> LoginResponse(null, null, input.name)
        }
    }
}