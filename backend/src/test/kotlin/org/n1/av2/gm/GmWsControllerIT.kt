package org.n1.av2.gm

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.n1.av2.integration.IntegrationTestBase
import org.n1.av2.integration.stomp.AvClient
import org.n1.av2.platform.connection.ServerActions

class GmWsControllerIT : IntegrationTestBase() {

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
