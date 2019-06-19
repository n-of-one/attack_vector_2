package org.n1.av2.backend.model.ui

class ValidationException(val errorMessage: String) : RuntimeException(errorMessage) {

    fun getNoty(): NotyMessage {
        return NotyMessage(NotyType.ERROR, "Invalid input", errorMessage)
    }
}