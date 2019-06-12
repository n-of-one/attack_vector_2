package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.model.iam.UserPrincipal
import org.n1.mainframe.backend.service.user.HackerActivityService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/hacker/")
class HackerController(val hackerActivityService: HackerActivityService) {

    @PostMapping("/connectionCheck")
    fun state(principal: Principal): HackerActivityService.ConnectionCheckResponse {
        val userPrincipal = principal as UserPrincipal
        return hackerActivityService.checkConnection(userPrincipal)
    }
}