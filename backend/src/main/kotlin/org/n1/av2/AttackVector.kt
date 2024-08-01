package org.n1.av2

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration




@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
class AttackVector

fun main(args: Array<String>) {
    SpringApplication.run(AttackVector::class.java, *args)
}
