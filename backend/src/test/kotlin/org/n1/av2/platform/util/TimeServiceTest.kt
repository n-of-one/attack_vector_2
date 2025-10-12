package org.n1.av2.platform.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeServiceTest {

    @Test
    fun `used at today 9_00, now its 10_00, expect isPastReset=false`() {
        val timeService = timeServiceWithTime(TODAY, 10)
        val result = timeService.isPastReset(TODAY.withHour(9))
        assertThat(result).isFalse
    }

    @Test
    fun `used at today 5_00, now its 10_00, expect isPastReset=true`() {
        val timeService = timeServiceWithTime(TODAY, 10)
        val result = timeService.isPastReset(TODAY.withHour(5))
        assertThat(result).isTrue
    }

    @Test
    fun `used at today 2_00, now its 3_00, expect isPastReset=false`() {
        val timeService = timeServiceWithTime(TODAY, 3)
        val result = timeService.isPastReset(TODAY.withHour(2))
        assertThat(result).isFalse
    }


    @Test
    fun `used yesterday at 3_00, now its 2_00, expect isPastReset=true`() {
        val timeService = timeServiceWithTime(TODAY, 2)
        val result = timeService.isPastReset(YESTERDAY.withHour(3))
        assertThat(result).isTrue
    }

    @Test
    fun `used yesterday at 9_00, now its 2_00, expect isPastReset=false`() {
        val timeService = timeServiceWithTime(TODAY, 2)
        val result = timeService.isPastReset(YESTERDAY.withHour(9))
        assertThat(result).isFalse
    }

    @Test
    fun `used yesterday at 9_00, now its 10_00, expect isPastReset=true`() {
        val timeService = timeServiceWithTime(TODAY, 10)
        val result = timeService.isPastReset(YESTERDAY.withHour(9))
        assertThat(result).isTrue
    }

    @Test
    fun `isCurrentPayoutDate today at 7_00`() {
        val timeService = timeServiceWithTime(TODAY, 7)
        val result = timeService.currentPayoutDate()
        assertThat(result).isEqualTo(TODAY.toLocalDate())
    }

    @Test
    fun `isCurrentPayoutDate today at 5_00`() {
        val timeService = timeServiceWithTime(TODAY, 5)
        val result = timeService.currentPayoutDate()
        assertThat(result).isEqualTo(TODAY.toLocalDate().minusDays(1))
    }

    @Test
    fun `isCurrentPayoutDate tomorrow at 5_00`() {
        val timeService = timeServiceWithTime(TODAY.plusDays(1), 5)
        val result = timeService.currentPayoutDate()
        assertThat(result).isEqualTo(TODAY.toLocalDate())
    }

    private fun timeServiceWithTime(day: ZonedDateTime, hour: Int): TimeService {
        val fixedTime = Clock.fixed(day.withHour(hour).toInstant(), UTC)
        return TimeService(fixedTime)
    }

    companion object {
        val UTC = ZoneId.of("UTC")
        val TODAY = ZonedDateTime.of(2025, 6, 23, 0, 0, 0, 0, UTC)
        val YESTERDAY = ZonedDateTime.of(2025, 6, 22, 0, 0, 0, 0, UTC)
    }

}
