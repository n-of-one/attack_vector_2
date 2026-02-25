package org.n1.av2.run.timings

import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.springframework.stereotype.Service

const val DEFAULT_AT_SPEED_4 = 4.0

/**
 * The timings are used to synchronize the frontend and backend.
 *
 * The timings in this class were originally created at what is now known as speed 4. This corresponds to showing 4 characters in a terminal every tick (50 ms).
 */
@Service
class TimingsService(
    private val skillService: SkillService,
    private val configService: ConfigService,
) {
    var MOVE_START = Timings("main" to 30)
    var MOVE_ARRIVE = Timings("main" to 8)

    var OUTSIDE_SCAN = Timings("out" to 50, "in" to 25, "connection" to 20)
    var INSIDE_SCAN = Timings("in" to 50, "out" to 25)

    // The value for "main" is composed of the total time of all messages shown when starting the attack. See: RunServerActionProcessor.ts
    // The time for move is the duration of the move animation.
    var START_ATTACK_SLOW = Timings("main" to 180, "fast" to 10, "medium" to 20, "slow" to 30, "move" to 135)
    var START_ATTACK_FAST = Timings("main" to 0)

    // used for unit tests and local development
    fun minimize() {
        MOVE_START = Timings("main" to 10)
        MOVE_ARRIVE = Timings("main" to 10)

        INSIDE_SCAN = Timings("in" to 0, "out" to 1)
        OUTSIDE_SCAN = Timings("out" to 0, "in" to 1, "connection" to 1)

        START_ATTACK_FAST = Timings("main" to 1)
        START_ATTACK_SLOW = Timings("main" to 1)
    }

    fun skillAndConfigAdjusted(timings: Timings, userId: String): Timings {
        val speed = skillService.skillAsIntOrNull(userId, SkillType.ADJUSTED_SPEED) ?: configService.getAsInt(ConfigItem.HACKER_DEFAULT_SPEED)
        val speedFactor = DEFAULT_AT_SPEED_4 / speed

        return timings.adjustedForSpeed(speedFactor)
    }
}
