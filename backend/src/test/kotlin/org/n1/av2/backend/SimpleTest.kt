package org.n1.av2.backend

import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class SimpleTest {

    @Test
    fun test() {

        val now = ZonedDateTime.now(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val result = now.format(formatter)
        println(result)
    }


}