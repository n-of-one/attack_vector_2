package org.n1.av2.frontend.web.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    ) {

    @GetMapping("/api/health")
    fun state(): String {
        return "ok"
    }
}
