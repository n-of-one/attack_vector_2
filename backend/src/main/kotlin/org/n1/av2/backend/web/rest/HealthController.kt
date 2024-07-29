package org.n1.av2.backend.web.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthController(
    ) {

    @GetMapping("/health")
    fun state(): String {
        return "ok"
    }
}