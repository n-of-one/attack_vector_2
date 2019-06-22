package org.n1.av2.backend.model

data class Syntax(val main: List<String>, val rest: String) {

    constructor(first: String, rest: String): this(listOf<String>(first), rest)
    constructor(first: String, second: String, rest: String): this(listOf<String>(first, second), rest)

}