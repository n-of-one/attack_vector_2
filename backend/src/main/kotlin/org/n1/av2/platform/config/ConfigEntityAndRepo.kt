package org.n1.av2.platform.config

import org.n1.av2.platform.util.validateDuration
import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


enum class ConfigItem(
    val defaultValue: String,
    val validate: ((String, Map<ConfigItem, String>) -> String?)? = null,
    val message: ((String) -> String?)? = null,
) {
    LARP_NAME("unknown", ::notEmpty),

    HACKER_SHOW_SKILLS("false", ::validBoolean),
    HACKER_EDIT_USER_NAME("true", ::validBoolean),
    HACKER_EDIT_CHARACTER_NAME("true", ::validBoolean),
    HACKER_DELETE_RUN_LINKS("true", ::validBoolean),
    HACKER_TUTORIAL_SITE_NAME(""),
    HACKER_SCRIPT_RAM_REFRESH_DURATION("00:15:00", ::validDuration),
    HACKER_SCRIPT_LOCKOUT_DURATION("01:00:00", ::validDuration),
    HACKER_SCRIPT_LOAD_DURING_RUN("false", ::validBoolean),
    HACKER_DEFAULT_SPEED("4", ::validHackerSpeed),

    LOGIN_PATH("/login", ::notEmpty, ::loginPathMessage),
    LOGIN_PASSWORD("", ::validPassword, ::loginPasswordMessage),
    LOGIN_GOOGLE_CLIENT_ID(""),

    DEV_HACKER_RESET_SITE("false", ::validBoolean),
    DEV_HACKER_USE_DEV_COMMANDS("false", ::validBoolean),
    DEV_MINIMUM_SHUTDOWN_DURATION("00:01:00", ::validDuration),
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS("0", ::validDelay),
    DEV_TESTING_MODE("false", ::validBoolean),
    DEV_QUICK_PLAYING("false", ::validBoolean),

    LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN(""),
    LARP_SPECIFIC_FRONTIER_LOLA_ENABLED("false", ::validBoolean),
}

data class ConfigEntry(
    @Id val item: ConfigItem,
    val value: String
)

@Repository
interface ConfigEntryRepo : CrudRepository<ConfigEntry, ConfigItem> {
    fun findByItem(type: ConfigItem): ConfigEntry?
}

@Suppress("unused")
fun notEmpty(toValidate: String, configValues: Map<ConfigItem, String>) = if (toValidate.isBlank()) "Value cannot be empty" else null

@Suppress("unused")
fun validBoolean(toValidate: String, configValues: Map<ConfigItem, String>) =
    when (toValidate.lowercase()) {
        "true", "false" -> null
        else -> "Value must be 'true' or 'false'"
    }

@Suppress("unused")
fun validHackerSpeed(toValidate: String, configValues: Map<ConfigItem, String>): String? {
    val value = toValidate.toIntOrNull()
    return when {
        value == null -> "Value must be an whole number > 0."
        value < 1 -> "Value must be 1 or higher."
        else -> null
    }
}

@Suppress("unused")
fun validDelay(toValidate: String, configValues: Map<ConfigItem, String>): String? {
    val value = toValidate.toLongOrNull()
    return when {
        value == null -> "Value must be an whole number (not excessively big)."
        value < 0 -> "Value must be 0 or positive"
        value > 1000 -> "Value must be less than 1000 (1 second), otherwise system becomes so unresponsive that it's hard to fix this configuration."
        else -> null
    }
}

fun validPassword(toValidate: String, configValues: Map<ConfigItem, String>): String? {
    return if (configValues[ConfigItem.LOGIN_PATH] == "/devLogin" && toValidate.isNotEmpty()) {
        "The login path is set to /devLogin. This means that the password will not be checked. If you want to set a password, also set the Login Path to /login."
    } else null
}

@Suppress("unused")
fun validDuration(toValidate: String, configValues: Map<ConfigItem, String>) = toValidate.validateDuration()

fun loginPathMessage(value: String): String? {
    if (value != "/login" && value != "/devLogin") {
        return "Non standard login path. Do not close this window. Use an incognito browser window to check that login works correctly."
    } else return null
}

@Suppress("unused")
fun loginPasswordMessage (ignored: String) =
    "Please check that you can login using the new password. Do not close this window, but open an incognito browser window to do this."


