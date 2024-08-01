package org.n1.av2.integration

import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.n1.av2.integration.service.WebsocketSiteService
import org.n1.av2.integration.stomp.HackerClient
import org.n1.av2.integration.stomp.StompClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import kotlin.random.Random

//@AutoConfigureDataMongo
//@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)


@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc

@Disabled // don't run this test by default, it's very slow. Also, currently AutoConfigureDataMongo is not working, so you need to manually start a MongoDB instance
class WebsocketTest {

    @Autowired
    private lateinit var stompClientService: StompClientService

    @Autowired
    private lateinit var websocketSiteService: WebsocketSiteService

    @LocalServerPort
    private val port = 0

    private val random = Random(System.currentTimeMillis())

    @BeforeAll
    fun setup() {
        stompClientService.setPort(port)
    }

    @Test
    fun testWebSocketCommunication(): Unit = runBlocking {

        val followerCount = 1
        websocketSiteService.importTestSite("v1-dev-websocket-test.json")
        websocketSiteService.makeHackable("dev-websocket-test-1")

        val followers = (1..followerCount).map { stompClientService.createAndConnect("$it") }
        val leader = stompClientService.createAndConnect("leader")

        leader.startRun("dev-websocket-test-1")
        leader.scan()

        followers.forEach { follower ->
            leader.shareSiteWith(follower)
        }

        followers.all { follower ->
            follower.waitForSiteInfo("dev-websocket-test-1")
            follower.joinRun()
            follower.startAttack()
        }

        val moves = 1

        (1..moves).forEach {
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
                    delay(random.nextLong(1000))
                    action(follower)
                }
            }
            jobs.joinAll()
        }
    }


}

