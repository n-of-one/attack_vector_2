package org.n1.av2.platform.util

import java.time.ZonedDateTime
import java.time.Duration

class CacheEntity<T>(val key: String, val value: T, val expiry: ZonedDateTime)

class Cache<T>(
    private val expiryDuration: Duration
) {
    private val cache = mutableMapOf<String, CacheEntity<T>>()

    fun get(key: String, default: T): T {
        val entity = cache[key] ?: return default
        if (entity.expiry.isBefore(ZonedDateTime.now())) {
            cache.remove(key)
            return default
        }
        return entity.value
    }

    fun set(key: String, value: T) {
        cache[key] = CacheEntity(key, value, ZonedDateTime.now().plus(expiryDuration))
    }

}
