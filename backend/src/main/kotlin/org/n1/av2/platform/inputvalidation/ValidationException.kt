package org.n1.av2.platform.inputvalidation

import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType

class ValidationException(val errorMessage: String) : RuntimeException(errorMessage) {

    fun getNoty(): NotyMessage {
        return NotyMessage(NotyType.ERROR, "Invalid input", errorMessage)
    }
}
