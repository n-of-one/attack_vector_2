package org.n1.av2.backend.model

/**
 * A Syntax has this form:
 *
 * "hack" -> Syntax("u", "primary", "error s")
 *
 * This translates to
 *
 * Syntax {
 * main: ["u", "primary"],
 * rest: "error s"
 * }
 *
 * This means:
 * - the first word will be underlined (in this case the command "hack"
 * - the second word will be marked with style primary, the name of the site to hack
 * - any additional parameters will be marked with styles error (red), s (strikethrough) to mark them as not useful
 */

data class Syntax(val main: List<String>, val rest: String) {

    constructor(first: String, rest: String): this(listOf<String>(first), rest)
    constructor(first: String, second: String, rest: String): this(listOf<String>(first, second), rest)

}