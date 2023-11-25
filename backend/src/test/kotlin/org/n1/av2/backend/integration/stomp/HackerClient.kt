package org.n1.av2.backend.integration.stomp

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.site.ScanInfoService

class HackerClient(
    private val client: AvClient,
    val userName: String,
) {

    private lateinit var siteId: String
    private lateinit var runId: String
    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerKotlinModule()


    suspend fun waitForConnection() {
        client.waitForConnection()
    }

    suspend fun connectTo(siteId: String, runId: String) {
        this.siteId = siteId
        this.runId = runId

        client.subscribe("/topic/run/${runId}")
        client.subscribe("/site/run/${siteId}")
        client.send("/av/scan/enterScan", runId)

        client.waitFor(ServerActions.SERVER_ENTER_RUN, "\"runId\":\"${runId}\"")
        client.clearMessage()
    }

    fun startAttack() {
        sendCommand("attack")
    }

    fun sendCommand(command:String) {
        client.send("/av/terminal/main", "{\"command\":\"${command}\",\"runId\":\"${runId}\"}")
    }

    suspend fun waitForAttackArrive() {
        val nodeId = "node-679a-4033"

        client.waitFor(ServerActions.SERVER_HACKER_MOVE_ARRIVE, "\"nodeId\":\"${nodeId}\",\"userId\":\"${client.userId}\"", 10)
        client.clearMessage()
    }

    fun move(networkId: String) {
        client.send("/av/terminal/main", "{\"command\":\"move ${networkId}\",\"runId\":\"${runId}\"}")
    }

    suspend fun waitForMoveArrive() {
        client.waitFor(ServerActions.SERVER_HACKER_MOVE_START, "\"userId\":\"${client.userId}\"", 1)
        client.waitFor(ServerActions.SERVER_HACKER_MOVE_ARRIVE, "\"nodeId\":\"node-abbc-4262\",\"userId\":\"${client.userId}\"", 3)
        client.clearMessage()
    }

    suspend fun removeUserScans() {
        client.send("/av/scan/scansOfPlayer", "")

        val userScansJson = client.waitFor(ServerActions.SERVER_UPDATE_USER_SCANS, "")
        val userScans: List<ScanInfoService.ScanInfo> = objectMapper.readValue(userScansJson)

        userScans.forEach { scanInfo ->
            client.send("/av/scan/deleteScan", scanInfo.runId)
        }

        client.waitFor(ServerActions.SERVER_UPDATE_USER_SCANS, "[]")
        client.clearMessage()
    }

    suspend fun enterAndScan(siteName: String) {
        client.send("/av/scan/scanForName", siteName)
        val siteJson = client.waitFor(ServerActions.SERVER_SITE_DISCOVERED, "")
        val siteInfo = objectMapper.readValue<Map<String, String>>(siteJson)
        val siteId = siteInfo["siteId"]!!
        val runId = siteInfo["runId"]!!

        connectTo(siteId, runId)

        sendCommand("scan 00")

        client.waitFor(ServerActions.SERVER_DISCOVER_NODES, "", 10)
        client.clearMessage()
    }

    suspend fun shareSiteWith(follower: HackerClient) {
        sendCommand("/share ${follower.userName}")
        client.waitFor(ServerActions.SERVER_TERMINAL_RECEIVE, "")
        client.clearMessage()
    }


}