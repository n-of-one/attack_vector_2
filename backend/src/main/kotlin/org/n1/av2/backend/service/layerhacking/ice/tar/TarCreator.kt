package org.n1.av2.backend.service.layerhacking.ice.tar

import org.n1.av2.backend.entity.site.enums.IceStrength

/*
progress in units, by hacker level:
1:  11 units / s
2:  12 units / s
3:  13 units / s
4:  14 units / s
5:  15 units / s
6:  16 units / s
7:  17 units / s
8:  18 units / s
9:  19 units / s
10: 20 units / s

Hacking crew: 10 + 10 + 9 + 8 + 8 ~ 95 units / s

*/




const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = MINUTE_SECONDS * 60

class TarCreator {

    companion object {

        private const val AVERAGE_UNITS_PER_SECOND = 15
        val totalUnitsByStrength = mapOf(
            IceStrength.VERY_WEAK to AVERAGE_UNITS_PER_SECOND * MINUTE_SECONDS * 10,  // 10 minutes  : 1.6 minutes : 9000
            IceStrength.WEAK to AVERAGE_UNITS_PER_SECOND * MINUTE_SECONDS * 30,       // 30 minutes  : 5 minutes   : 27000
            IceStrength.AVERAGE to AVERAGE_UNITS_PER_SECOND * HOUR_SECONDS * 1,       // 1 hour      : 10 minutes  : 54000
            IceStrength.STRONG to AVERAGE_UNITS_PER_SECOND * HOUR_SECONDS * 6,        // 6 hours     : 1 hours     : 324000
            IceStrength.VERY_STRONG to AVERAGE_UNITS_PER_SECOND * HOUR_SECONDS * 24,  // 24 hours    : 4 hours     : 1296000
            IceStrength.ONYX to AVERAGE_UNITS_PER_SECOND * HOUR_SECONDS * 144         // 144 hours   : 24 hours    : 7776000
        )

        fun defaultTimeHackerGroup(strength: IceStrength, hackerLevel: Int, hackerCount: Int ): String {
            val units = totalUnitsByStrength[strength] ?: error("No Ice units defined for strength: ${strength}")
            val seconds = units / unitsPerSecond(hackerLevel, hackerCount)
            return secondsToTime(seconds)
        }

        fun unitsPerSecond(hackerLevel: Int, hackerCount: Int = 1): Int {
            val baseUnitsPerSecond = 10 + hackerLevel
            return baseUnitsPerSecond * hackerCount
        }


        private fun secondsToTime(seconds: Int): String {
            val hours = seconds / HOUR_SECONDS
            val minutes = (seconds % HOUR_SECONDS) / MINUTE_SECONDS
            val remainingSeconds = seconds % MINUTE_SECONDS

            if (hours == 0 && minutes == 0) return "${remainingSeconds}s"
            if (hours == 0) return "${minutes}m ${remainingSeconds}s"
            return "${hours}h ${minutes}m ${remainingSeconds}s"
        }

    }



    fun create(strength: IceStrength): Int {
        return totalUnitsByStrength[strength] ?: error("No Ice units defined for strength: ${strength}")
    }
}

