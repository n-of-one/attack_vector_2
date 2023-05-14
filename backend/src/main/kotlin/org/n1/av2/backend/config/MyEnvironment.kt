package org.n1.av2.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MyEnvironment(
    @Value("\${ENVIRONMENT:default}") private val environment: String
) {

    val dev: Boolean
        get() = (environment.startsWith("dev"))
}
