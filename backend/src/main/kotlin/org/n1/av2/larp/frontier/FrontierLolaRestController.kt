package org.n1.av2.larp.frontier

import org.n1.av2.platform.iam.authentication.JwtTokenProvider
import org.n1.av2.platform.iam.user.UserEntityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class FrontierLolaRestController(
    private val userEntityService: UserEntityService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val lolaService: LolaService,
) {

    @GetMapping("/api/admin/frontier/lola/token")
    fun getLolaToken(): String {
        val lolaUser = userEntityService.getByName("Lola")

        val now = Date()
        val yearInMs = 1000L * 60 * 60 * 24 * 365
        val expiry = Date(now.time + yearInMs)

        return jwtTokenProvider.generateJwt(lolaUser, expiry)
    }

    @GetMapping("/api/admin/frontier/lola/speak")
    fun lolaSay(@RequestParam text: String): LolaService.SpeakResponse {
        val response = lolaService.speak(text)
        return response
    }

}
