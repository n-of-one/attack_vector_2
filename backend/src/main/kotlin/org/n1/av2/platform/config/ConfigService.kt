package org.n1.av2.platform.config

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.validateDuration
import org.springframework.stereotype.Service
import java.time.Duration
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

    fun setAndReply(item: ConfigItem, value: String) {
        set(item, value)

        replyConfigValues()
        replySpecificConfigValue(item, value)
    }

    fun set(item: ConfigItem, value: String) {
        checkValue(item, value)
        val entry = repo.findByItem(item)?.copy(value = value) ?: ConfigEntry(item, value)
        repo.save(entry)
        cache[item] = value
    }

    private fun replySpecificConfigValue(item: ConfigItem, value: String) {
        val messageFunction = item.message ?: return // no message function
        val message = messageFunction(value) ?: return // no message

        connectionService.replyError(message)
    }

    private fun checkValue(item: ConfigItem, value: String) {
        val validationFunction = item.validate ?: return // no validation function
        val errorMessage = validationFunction(value, cache) ?: return // no error

        replyConfigValues()
        throw ValidationException(errorMessage)
    }

    fun getAsBoolean(item: ConfigItem): Boolean {
        val stringValue = get(item)
        return stringValue.toBoolean()
    }

    fun getAsLong(item: ConfigItem): Long {
        val stringValue = get(item)
        return stringValue.toLong()
    }

    fun getAsDuration(item: ConfigItem): Duration {
        val stringValue = get(item)
        if (stringValue.validateDuration() != null ) error("Duration invalid for config item: $item")
        return stringValue.toDuration()
    }
}
