package org.n1.av2.backend.integration

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.n1.av2.backend.integration.stomp.HackerClient
import org.n1.av2.backend.integration.stomp.StompClientService
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

        // FIXME auto-create a site with two nodes.

        val followerCount = 10

        val followers = (1..followerCount).map { stompClientService.createAndConnect("$it") }

        val leader = stompClientService.createAndConnect("leader")
        leader.startRun("test")
        leader.scan()

        followers.forEach { follower ->
            leader.shareSiteWith(follower)
        }


        followers.all { follower ->
            follower.waitForSiteInfo("test")
            follower.joinRun()
            follower.startAttack()
        }

        (1..2).forEach {
            moveBackAndForth(followers)
        }
    }

    private suspend fun moveBackAndForth(followers: List<HackerClient>) {
        followers.all { follower ->
            follower.move("01")
        }
        followers.all { follower ->
            follower.move("00")
        }
    }


    suspend fun List<HackerClient>.all(action: suspend (HackerClient) -> Unit) {
        val followers = this

        runBlocking {
            val jobs = followers.map { follower ->
                launch {
                    action(follower)
                }
            }
            jobs.joinAll()
        }
    }


}

