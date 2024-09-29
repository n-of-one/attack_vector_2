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
    HACKER_CREATE_SITES("false"),

    LOGIN_PATH( "/login",),
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

/*
   HACKER_SHOW_SKILLS("Hackers", "show skills", "false", "Show the skills of a hacker to the hacker"),
    HACKER_EDIT_USER_NAME("Hackers", "edit user name", "true", "Allow the hacker to edit their user name"),
    HACKER_EDIT_CHARACTER_NAME("Hacker", "edit character name", "true", "Allow the hacker to edit their character name"),
    HACKER_DELETE_RUN_LINKS("Hacker", "delete run link", "true", "Allow the hacker to delete run links, this is default functionality. It's disabled for attackvector.nl"),
    HACKER_CREATE_SITES("Hacker", "create site", "false", "Allow the hacker to create sites"),
    SKILL_SEARCH_SITE_DEFAULT("Skill", "search site default", "true", "Do all hackers by default have the skill to search for sites? If not, those hackers cannot start a new hack, and must be invited to a site by another hacker"),
    SKILL_SCAN_DEFAULT("Skill", "scan default", "true", "Do all hackers by default have the skill to scan? If not, those hackers cannot scan a site, but will only discover one node at a time."),
    LOGIN_URL("Login", "URL", "/login", "Override in case the larp uses an SSO"),
    LOGIN_ADMIN_PASSWORD("Login", "Admin password", "not_set", "The GM password. If set to 'not_set' you cannot login as GM or admin. If set to 'not_needed' no password is needed, and a special login page is shown. Useful for development."),
    LOGIN_GOOGLE_CLIENT_ID("Login", "Google client ID", "not used", "The google client id"),
    DEV_SIMULATE_NON_LOCALHOST("Dev", "simulate non localhost", "false", "Development option: add a delay to simulate AV running in the cloud, to get a better feel for how responsive AV will be in practice"),
    DEV_HACKER_RESET_SITE("Dev", "allow hacker to reset site", "false", "Development option: allow the hacker to reset the site. This is useful when testing as a developer"),
    DEV_QUICK_PLAYING("Dev", "quick playing", "false", "Development option: set to true for quicker start of ice puzzles"),
    DEV_HACKER_ADMIN_COMMANDS("Dev", "hacker admin commands", "false", "Allow hackers to user admin commands like 'quickattack'. Usefull for development"),
    INSTALLATION_LARP_NAME("Installation", "larp name", "unknown", "Used for very larp specific functionality, such as larp specific login"),
    INSTALLATION_ENVIRONMENT("Installation", "environment", "dev", "Name for this installation. Used to label your site exports. For example: dev, prod"),
    INSTALLATION_TIME_ZONE("Installation", "time zone", "default", "The time zone is used when exporting sites. Example value: \"Europe/ Paris\" \"default\" means use the server time zone. "),
    INSTALLATION_LOCAL_CONTENT_FOLDER("Installation", "local content folder", "local", """If you want to locally host content that will be available for hackers to discover, you can create this folder in the folder where your attack vector
        startup script resided. Then you can access the files of that folder via the path: /local/{filename} . For example: for the site attackvector.nl if
    you create a folder 'local' with a 'file info.txt' then you can access it via the URL: https://attackvector.nl/local/info.txt . You can also create subdirectories if you want.
    The purpose of local files is that you can link to them from text-layers. This way the hackers can discover the files when hacking these layers.),"""),

 */
