package org.n1.av2.backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SimpleTest {


    data class Played(val heart: Boolean)

    data class Oops(val did: String, val player: Played?)
    @Test
    fun testDeserialize() {

        val input = """{ "did": "again" }"""

//        val gson = Gson()
//        val britney = gson.fromJson(input, Oops::class.java)

        val objectMapper = ObjectMapper().registerKotlinModule()
        val britney:Oops = objectMapper.readValue(input)

        println(britney)

        Assertions.assertEquals("again", britney.did)

        val complex = """{ "did": "again", "played": { "heart": true } }"""

        val map = objectMapper.readValue<Map<String, Any>>(complex)
        println(map["did"])
        println((map["played"] as Map<*, *>)["heart"])

    }
}