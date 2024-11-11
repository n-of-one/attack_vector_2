package org.n1.av2.platform.config

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


enum class ConfigItem(
    val defaultValue: String,
    val validate: ((String, Map<ConfigItem, String>) -> String?)? = null,
    val message: ((String) -> String?)? = null,
) {
    LARP_NAME("unknown", notEmpty),

    HACKER_SHOW_SKILLS("false", isBoolean),
    HACKER_EDIT_USER_NAME("true", isBoolean),
    HACKER_EDIT_CHARACTER_NAME("true", isBoolean),
    HACKER_DELETE_RUN_LINKS("true", isBoolean),
    HACKER_TUTORIAL_SITE_NAME(""),

    LOGIN_PATH("/login", notEmpty, loginPathMessage),
    LOGIN_PASSWORD("", validPassword, loginPasswordMessage),
    LOGIN_GOOGLE_CLIENT_ID(""),

    DEV_HACKER_RESET_SITE("false", isBoolean),
    DEV_QUICK_PLAYING("false", isBoolean),
    DEV_HACKER_USE_DEV_COMMANDS("false", isBoolean),
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS("0", validDelay),

    LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN(""),
    LARP_SPECIFIC_FRONTIER_LOLA_ENABLED("false", isBoolean),
}

data class ConfigEntry(
    @Id val item: ConfigItem,
    val value: String
)

@Repository
interface ConfigEntryRepo : CrudRepository<ConfigEntry, ConfigItem> {
    fun findByItem(type: ConfigItem): ConfigEntry?
}


val notEmpty = { toValidate: String, configValues: Map<ConfigItem, String> -> if (toValidate.isBlank()) "Value cannot be empty" else null }
val isBoolean = { toValidate: String, configValues: Map<ConfigItem, String> ->
    when (toValidate.lowercase()) {
        "true", "false" -> null
        else -> "Value must be 'true' or 'false'"
    }
}
val validDelay = { toValidate: String, configValues: Map<ConfigItem, String> ->
    val value = toValidate.toLongOrNull()
    when {
        value == null -> "Value must be an whole number (not excessively big)."
        value < 0 -> "Value must be 0 or positive"
        value > 1000 -> "Value must be less than 1000 (1 second), otherwise system becomes so unresponsive that it's hard to fix this configuration."
        else -> null
    }
}
val validPassword = { toValidate: String, configValues: Map<ConfigItem, String> ->
    if (configValues[ConfigItem.LOGIN_PATH] == "/devLogin" && toValidate.isNotEmpty()) {
        "The login path is set to /devLogin. This means that the password will not be checked. If you want to set a password, also set the Login Path to /login."
    } else null
}

val loginPathMessage = { value: String ->
    if (value != "/login" && value != "/devLogin") {
        "Non standard login path. Do not close this window. Use an incognito browser window to check that login works correctly."
    } else null
}

val loginPasswordMessage = { _: String ->
    "Please check that you can login using the new password. Do not close this window, but open an incognito browser window to do this."
}


