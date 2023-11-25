package org.n1.av2.backend.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.n1.av2.backend.integration.stomp.HackerClient
import org.n1.av2.backend.integration.stomp.StompClientService
import org.n1.av2.backend.service.site.ScanInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebsocketTest {

    @Autowired
    private lateinit var stompClientService: StompClientService

    @LocalServerPort
    private val port = 0

    @BeforeAll
    fun setup() {
        stompClientService.setPort(port)
    }



    @Test
    fun testWebSocketCommunication(): Unit = runBlocking {

        val followerCount = 2

        val followers = (1..followerCount).map { stompClientService.createAndConnect("$it") }
        followers.forEach {follower ->
            follower.removeUserScans()
        }

        val leader = stompClientService.createAndConnect("leader")
        leader.removeUserScans()
        leader.enterAndScan("test")


        followers.forEach {follower ->
            leader.shareSiteWith(follower)
        }


//        followers.forEach {follower ->
//            attackAndMove(follower, leader.siteId, leader.runId)
//        }





//


//        val hackers = listOf(
////            "Stalker",
////            "Shade_zero",
////        )
//        hackers.forEach {name ->
//            launch {
//                attackAndMove(name)
//                delay(1000)
//            }
//            delay(10)
//        }


//        Thread.sleep(1000)
//        println("before send")
//        avClient.send("/av/scan/scansOfPlayer", "")
//
//        Thread.sleep(1000)
    }

    private suspend fun attackAndMove(hacker: HackerClient) {
        hacker.connectTo("site-49c3-434b", "run-9ec8-447a")
        hacker.startAttack()
        hacker.waitForAttackArrive()
        hacker.move("01")
        hacker.waitForMoveArrive()
    }

}

