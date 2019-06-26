package org.n1.av2.backend.util

import mu.KLogger

val totalsByName = HashMap<String, Long>()
val countByName = HashMap<String, Int>()


inline fun <T>logNanoTime(name: String, logger: KLogger, block: () -> T): T {
    val start = System.nanoTime()
    val result = block()
    val time = System.nanoTime() - start
    val newTotal = totalsByName.getOrDefault(name, 0L) + time
    val newCount = countByName.getOrDefault(name, 0) + 1
    totalsByName[name] = newTotal
    countByName[name] = newCount

    val average = newTotal / newCount
    val averageFormatted = String.format("%,d", average)
    logger.debug("${name} : average: ${averageFormatted} ns (over ${newCount}, this: (${time})")
    return result
}