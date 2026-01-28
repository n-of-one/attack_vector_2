package org.n1.av2.run.timings

import org.springframework.stereotype.Service

/**
 * The timings are stored in a class to allow unit tests to change them to speed up unit testing.
 */
@Service
class TimingsService {
    var MOVE_START = Timings("main" to 30)
    var MOVE_ARRIVE = Timings("main" to 8)

    var OUTSIDE_SCAN = Timings("out" to 50, "in" to 25, "connection" to 20)
    var INSIDE_SCAN = Timings("in" to 50, "out" to 25)

    var START_ATTACK_FAST = Timings("main" to 0)
    var START_ATTACK_SLOW = Timings("main" to 190)

    // used for unit tests and local development
    fun minimize() {
        MOVE_START = Timings("main" to 10)
        MOVE_ARRIVE = Timings("main" to 10)

        INSIDE_SCAN = Timings("in" to 0, "out" to 1)
        OUTSIDE_SCAN = Timings("out" to 0, "in" to 1, "connection" to 1)

        START_ATTACK_FAST = Timings("main" to 1)
        START_ATTACK_SLOW = Timings("main" to 1)
    }
}
