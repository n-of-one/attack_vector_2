package org.n1.av2.hacker

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.n1.av2.integration.IntegrationTestBase
import org.n1.av2.integration.stomp.AvClient
import org.n1.av2.platform.connection.ServerActions

class HackerWsControllerIT : IntegrationTestBase() {

    @Test
    fun `verify hacker-logon success for hacker`(): Unit = runBlocking {
        val connection = stompClientService.connectUser("hacker")

        logonAndValidate(connection)
    }

    @Test
    fun `verify hacker-logon denied for gm`(): Unit = runBlocking {
        val connection = stompClientService.connectUser("gm")

        val exception = assertThrows<IllegalStateException> {
            logonAndValidate(connection)
        }
        assertTrue(exception.message?.contains("times out waiting for SERVER_SITES_LIST") ?: false)
    }


    @Test
    fun `verify hacker-logon denied for anonymous user`(): Unit = runBlocking {
        val connection = stompClientService.connectAnonymous()

        val exception = assertThrows<IllegalStateException> {
            logonAndValidate(connection)
        }
        assertTrue(exception.message?.contains("times out waiting for SERVER_SITES_LIST") ?: false)
    }


    private suspend fun logonAndValidate(connection: AvClient) {
        connection.send("/av/hacker/logon", "")
        connection.waitFor(ServerActions.SERVER_SITES_LIST, "")
    }

}
