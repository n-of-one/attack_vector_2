package org.n1.av2.platform.config

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class ConfigService(
    private val repo: ConfigEntryRepo,
    private val connectionService: ConnectionService,
) {


    private val logger = mu.KotlinLogging.logger {}

    val cache: MutableMap<ConfigItem, String> = HashMap()


    @PostConstruct
    fun logEnvironment() {
        logger.info("Larp: ${get(ConfigItem.LARP_NAME)}")
    }

    fun initConfigValues() {
        val allEntries = repo.findAll()
        val allItems: List<ConfigEntry> = ConfigItem.entries.map { item ->
            allEntries.find { it.item == item } ?: ConfigEntry(item, item.defaultValue)
        }
        updateCache(allItems)
    }

    fun initForTest(values: Map<ConfigItem, String>) {
        val allItems = ConfigItem.entries.map { item: ConfigItem ->
            val value = values[item] ?: item.defaultValue
            ConfigEntry(item, value)
        }
        updateCache(allItems)

    }

    private fun updateCache(allItems: List<ConfigEntry>) {
        val cacheValues = allItems.associate { it.item to it.value }.toMutableMap()
        cache.clear()
        cache.putAll(cacheValues)
    }

    fun replyConfigValues() {
        val allItems = cache.map { ConfigEntry(it.key, it.value) }
        connectionService.reply(ServerActions.SERVER_RECEIVE_CONFIG, allItems)
    }

    fun get(item: ConfigItem): String {
        if (cache.isEmpty()) initConfigValues()
        return cache[item] ?: error("Unknown config item $item")
    }

    fun set(item: ConfigItem, value: String) {
        checkValue(item, value)
        val entry = repo.findByItem(item)?.copy(value = value) ?: ConfigEntry(item, value)
        repo.save(entry)
        cache[item] = value

        replyConfigValues()
        replySpecificConfigValue(item, value)
    }

    private fun replySpecificConfigValue(item: ConfigItem, value: String) {
        when (item) {
            ConfigItem.LOGIN_PATH ->
                if (value != "/login" && value != "/devLogin") {
                    connectionService.replyError("Non standard login path. Do not close this window. Use an incognito browser window to check that login works correctly.")
                }
            ConfigItem.LOGIN_PASSWORD ->
                connectionService.replyError("Please check that you can login using the new password. Do not close this window, but open an incognito browser window to do this.")
            else -> return
        }
    }

    private fun checkValue(item: ConfigItem, value: String) {
        when (item) {
            ConfigItem.HACKER_SHOW_SKILLS,
                ConfigItem.HACKER_EDIT_USER_NAME,
                ConfigItem.HACKER_EDIT_CHARACTER_NAME,
                ConfigItem.HACKER_DELETE_RUN_LINKS,
                ConfigItem.DEV_HACKER_RESET_SITE,
                ConfigItem.DEV_QUICK_PLAYING,
                ConfigItem.DEV_HACKER_USE_DEV_COMMANDS -> {
                if (!"true".equals(value, ignoreCase = true) && !"false".equals(value, ignoreCase = true)) {
                    error("Value must be 'true' or 'false'")
                }
            }
            ConfigItem.DEV_SIMULATE_NON_LOCALHOST_DELAY_MS -> {
                try {
                    value.toLong()
                    return
                } catch (_: Exception) {
                    error("Value must be a number.")
                }
            }
            else -> {
                return
            }
        }
    }

    fun getAsBoolean(devHackerAdminCommands: ConfigItem): Boolean {
        val stringValue = get(devHackerAdminCommands)
        return stringValue.toBoolean()
    }

    fun getAsLong(devSimulateNonLocalhostDelayMs: ConfigItem): Long {
        val stringValue = get(devSimulateNonLocalhostDelayMs)
        return stringValue.toLong()
    }

}
