package org.n1.av2.backend.integration.stomp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.RunService
import org.n1.av2.run.runlink.RunLinkService

class HackerClient(
    private val client: AvClient,
    val userName: String,
) {

    private lateinit var siteId: String
    private lateinit var runId: String
    private lateinit var siteInfo: RunService.SiteInfo
    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerKotlinModule()



    suspend fun startAttack() {
        val node = this.siteInfo.site.nodes.find { it.networkId == "00" } ?: error("Could not find node with networkId \"00\"")

        println("\nStarting attack, waiting to arrive at 00 = ${node.id}")

        sendCommand("attack")

        client.waitFor(ServerActions.SERVER_HACKER_MOVE_ARRIVE, "\"nodeId\":\"${node.id}\",\"userId\":\"${client.userId}\"", 10)
        client.clearMessage()
    }

    fun sendCommand(command:String) {
        client.send("/av/terminal/main", "{\"command\":\"${command}\",\"runId\":\"${runId}\"}")
    }


    suspend fun removeUserScans() {
        client.send("/av/scan/scansOfPlayer", "")

        val userScansJson = client.waitFor(ServerActions.SERVER_UPDATE_USER_RUNS, "")
        val userScans: List<RunLinkService.RunInfo> = objectMapper.readValue(userScansJson)

        userScans.forEach { scanInfo ->
            client.send("/av/scan/deleteScan", scanInfo.runId)
        }

        client.waitFor(ServerActions.SERVER_UPDATE_USER_RUNS, "[]")
        client.clearMessage()
    }

    suspend fun startRun(siteName: String) {
        client.send("/av/run/newRun", siteName)
        waitForSiteInfo(siteName)
        joinRun()
    }

    suspend fun joinRun() {
        client.send("/av/run/prepareToEnterRun", runId)
        client.waitFor(ServerActions.SERVER_ENTERING_RUN, runId)

        client.subscribe("/topic/run/${runId}")
        client.subscribe("/site/run/${siteId}")
        client.send("/av/run/enterRun", runId)


        val siteInfoJson = client.waitFor(ServerActions.SERVER_ENTERED_RUN, "\"runId\":\"${runId}\"")
        this.siteInfo = objectMapper.readValue(siteInfoJson)
        client.clearMessage()
    }

    suspend fun waitForSiteInfo(siteName: String) {
        val sitesJson = client.waitFor(ServerActions.SERVER_UPDATE_USER_RUNS, """"siteName":"${siteName}"""")
        val sites: List<RunLinkService.RunInfo> = objectMapper.readValue(sitesJson)
        val site = sites.find { it.siteName == siteName } ?: error("Did not receive site with name ${siteName}, but got: ${sitesJson}")
        siteId = site.siteId
        runId = site.runId
    }


    suspend fun scan() {
        sendCommand("scan 00")

        client.waitFor(ServerActions.SERVER_DISCOVER_NODES, "", 10)
        client.clearMessage()
    }

    suspend fun shareSiteWith(follower: HackerClient) {
        sendCommand("/share ${follower.userName}")
        client.waitFor(ServerActions.SERVER_TERMINAL_RECEIVE, "")
        client.clearMessage()
    }

    suspend fun move(networkId: String) {
        val node = this.siteInfo.site.nodes.find { it.networkId == networkId } ?: error("Could not find node with networkId ${networkId}")

        println("\nMoving to node ${networkId} = ${node.id}")

        client.send("/av/terminal/main", "{\"command\":\"move ${networkId}\",\"runId\":\"${runId}\"}")

        client.waitFor(ServerActions.SERVER_HACKER_MOVE_START, "\"userId\":\"${client.userId}\"", 1)
        client.waitFor(ServerActions.SERVER_HACKER_MOVE_ARRIVE, "\"nodeId\":\"${node.id}\",\"userId\":\"${client.userId}\"", 3)
        client.clearMessage()
    }

}
