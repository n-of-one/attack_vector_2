package org.n1.av2.backend.util

import org.springframework.stereotype.Component

@Component
class FileNameUtil {

    // Remove any characters that are not allowed in file names: replace them with _
    // not allowed are: \ / : ; * ? " < > |
    // also not allowed: non-printable characters and dot
    fun makeSafe(name: String): String {
        return name.replace(Regex("[\\p{C}.\\\\:;*/<>|\"?]"), "_")
    }

}