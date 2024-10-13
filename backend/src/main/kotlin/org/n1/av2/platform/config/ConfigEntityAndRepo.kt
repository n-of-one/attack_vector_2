package org.n1.av2.platform.config

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


enum class ConfigItem(
    val defaultValue: String,
) {
    LARP_NAME( "unknown"),

    HACKER_SHOW_SKILLS("false"),
    HACKER_EDIT_USER_NAME("true"),
    HACKER_EDIT_CHARACTER_NAME("true"),
    HACKER_DELETE_RUN_LINKS("true"),

    LOGIN_PATH( "/login"),
    LOGIN_PASSWORD(""),
    LOGIN_GOOGLE_CLIENT_ID(""),

    DEV_HACKER_RESET_SITE("false"),
    DEV_QUICK_PLAYING("false"),
    DEV_HACKER_USE_DEV_COMMANDS("false"),
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS("0"),

    FRONTIER_ORTHANK_TOKEN(""),

}

data class ConfigEntry(
    @Id val item: ConfigItem,
    val value: String
)

@Repository
interface ConfigEntryRepo : CrudRepository<ConfigEntry, ConfigItem> {
    fun findByItem(type: ConfigItem): ConfigEntry?
}

