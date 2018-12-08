package org.n1.mainframe.backend.model.ui

class ValidationException(val errorMessage: String) : RuntimeException(errorMessage) {

    fun getNoty(): NotyMessage {
        return NotyMessage("error", "Invalid input", errorMessage)
    }
}