package org.n1.av2.backend.model.ui

import com.fasterxml.jackson.annotation.JsonValue

enum class NotyType(@get:JsonValue val code: String) {
    OK("ok_right"),
    NEUTRAL("neutral"),
    ERROR("error"),
    FATAL("fatal");

}