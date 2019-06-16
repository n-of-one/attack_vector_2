package org.n1.mainframe.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MyEnvironment(
        @Value("#{environment.ENVIRONMENT}") private val environment: String = "unspecified") {


    val dev: Boolean
        get() = true
//        get() = (environment.startsWith("dev"))
}