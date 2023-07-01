package org.n1.av2.backend.model.ui

import com.fasterxml.jackson.annotation.JsonValue

enum class NotyType(@get:JsonValue val code: String) {
    NEUTRAL("neutral"),
    ERROR("error"),
    FATAL("fatal");
}

data class NotyMessage(
    val type: NotyType,
    val title: String,
    val message: String
) {
    companion object {
        fun neutral(message: String) = NotyMessage(NotyType.NEUTRAL, "", message)
    }

}