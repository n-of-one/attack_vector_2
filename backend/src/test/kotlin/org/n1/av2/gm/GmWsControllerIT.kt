package org.n1.av2.gm

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.n1.av2.integration.stomp.AvClient
import org.n1.av2.integration.stomp.StompClientService
import org.n1.av2.platform.connection.ServerActions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GmWsControllerIT {

    @Autowired
    private lateinit var stompClientService: StompClientService

    @LocalServerPort
    private val port = 0


    @BeforeAll
    fun setup() {
        stompClientService.port = port
    }

    @Test
    fun `verify gm-logon success for gm`(): Unit = runBlocking {
        val connection = stompClientService.connectUser("gm")

        logonAndValidate(connection)
    }

    @Test
    fun `verify gm-logon denied for hacker`(): Unit = runBlocking {
        val connection = stompClientService.connectUser("hacker")

        val exception = assertThrows<IllegalStateException> {
            logonAndValidate(connection)
        }
        assertTrue(exception.message?.contains("times out waiting for SERVER_SITES_LIST") ?: false)
    }


    @Test
    fun `verify gm-logon denied for anonymous user`(): Unit = runBlocking {
        val connection = stompClientService.connectAnonymous()

        val exception = assertThrows<IllegalStateException> {
            logonAndValidate(connection)
        }
        assertTrue(exception.message?.contains("times out waiting for SERVER_SITES_LIST") ?: false)
    }


    private suspend fun logonAndValidate(connection: AvClient) {
        connection.send("/av/gm/logon", "")
        connection.waitFor(ServerActions.SERVER_SITES_LIST, "")
    }

}
