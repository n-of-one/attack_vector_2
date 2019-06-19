package org.n1.av2.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class AttackVector

fun main(args: Array<String>) {
    SpringApplication.run(AttackVector::class.java, *args)
}
