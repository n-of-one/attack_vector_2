package org.n1.av2.site.entity.enums

enum class IceStrength(val description: String, val value: Int) {
    VERY_WEAK("Very weak", 1),
    WEAK("Weak", 2),
    AVERAGE("Average", 3),
    STRONG("Strong", 4),
    VERY_STRONG("Very strong", 5),
    ONYX("Onyx", 6);

    companion object {
        fun forValue(value: Int): IceStrength {
            return entries.find {it.value == value} ?: error("No IceStrength exists for value: $value")
        }
    }
}
