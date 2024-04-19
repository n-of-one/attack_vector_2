package org.n1.av2.backend.web.rest

import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.larp.frontier.LolaService
import org.n1.av2.backend.service.util.StompService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/admin/frontier")
class FrontierAdminRestController(
    private val userEntityService: UserEntityService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val stompService: StompService,
    private val lolaService: LolaService,
) {

    @GetMapping("/lola/token")
    fun getLolaToken(): String {
        val lolaUser = userEntityService.getByName("Lola")

        val now = Date()
        val yearInMs = 1000 * 60 * 60 * 24 * 365;
        val expiry = Date(now.time + yearInMs)

        return jwtTokenProvider.generateJwt(lolaUser, expiry)
    }

    @GetMapping("/lola/speak")
    fun lolaSay(@RequestParam text: String): LolaService.SpeakResponse {
        val response = lolaService.speak(text)
        return response
    }

}