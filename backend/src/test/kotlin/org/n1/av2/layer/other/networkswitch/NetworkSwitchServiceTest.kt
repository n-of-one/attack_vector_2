package org.n1.av2.layer.other.networkswitch

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.OPEN
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.PENDING
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.DROPPED
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.Connection
import org.n1.av2.site.entity.ConnectionEntityService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.entity.enums.NodeType

private const val SITE_ID = "site-1"
private const val SWITCH_LAYER_ID = "n01:layer-1"

/*
 Test site, 00 is the start node, 01 has the network switch with routes east (02) and west (03).
 The branches join again at 04.

        +-- 02 --+
 00 -- 01        04
        +-- 03 --+
*/
@ExtendWith(MockKExtension::class)
class NetworkSwitchServiceTest {

    @MockK
    lateinit var nodeEntityService: NodeEntityService

    @MockK
    lateinit var connectionEntityService: ConnectionEntityService

    @MockK
    lateinit var sitePropertiesEntityService: SitePropertiesEntityService

    @MockK
    lateinit var connectionService: ConnectionService

    @MockK
    lateinit var hackerStateEntityService: HackerStateEntityService

    @MockK
    lateinit var runService: RunService

    @InjectMockKs
    lateinit var networkSwitchService: NetworkSwitchService

    private lateinit var switchLayer: NetworkSwitchLayer
    private lateinit var nodes: List<Node>

    private val connections = listOf(
        connection("c00-01", "n00", "n01"),
        connection("c01-02", "n01", "n02"),
        connection("c01-03", "n01", "n03"),
        connection("c02-04", "n02", "n04"),
        connection("c03-04", "n03", "n04"),
    )

    @BeforeEach
    fun setup() {
        switchLayer = NetworkSwitchLayer(
            SWITCH_LAYER_ID, 1, "Network switch", "",
            mutableListOf(NetworkSwitchRoute("east", "n02"), NetworkSwitchRoute("west", "n03")), null
        )
        nodes = listOf(node("n00"), node("n01", switchLayer), node("n02"), node("n03"), node("n04"))

        every { nodeEntityService.findBySiteId(SITE_ID) } answers { nodes }
        every { nodeEntityService.findByLayerId(SWITCH_LAYER_ID) } answers { nodes[1] }
        every { nodeEntityService.save(any()) } just runs
        every { connectionEntityService.getAll(SITE_ID) } returns connections
        every { sitePropertiesEntityService.getBySiteId(SITE_ID) } returns
            SiteProperties(siteId = SITE_ID, name = "test", ownerUserId = "owner", startNodeNetworkId = "00")
        every { connectionService.replyTerminalReceive(any<List<String>>()) } just runs
        every { connectionService.replyTerminalReceive(*anyVararg<String>()) } just runs
        every { connectionService.toSite(any(), any(), *anyVararg()) } just runs
        every { hackerStateEntityService.findAllHackersInSite(SITE_ID) } returns emptyList()
        every { runService.hackerDisconnect(any(), any()) } just runs
        every { runService.makeNeighborConnectable(any(), any()) } just runs
    }

    private fun hackerAt(userId: String, nodeId: String, activity: HackerActivity = HackerActivity.INSIDE) =
        HackerState(userId, "connection", "run-1", SITE_ID, nodeId, null, activity, null, null, null)

    private fun node(id: String, vararg layers: NetworkSwitchLayer) =
        Node(id, SITE_ID, NodeType.TRANSIT_2, 0, 0, layers.toMutableList(), id.removePrefix("n"))

    private fun connection(id: String, fromId: String, toId: String) = Connection(id, SITE_ID, fromId, toId)

    @Test
    fun `routes are pending until a choice is made`() {
        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(statuses).containsEntry("c00-01", OPEN)
            .containsEntry("c01-02", PENDING)
            .containsEntry("c01-03", PENDING)
            .containsEntry("c02-04", OPEN)
    }

    @Test
    fun `choice opens the chosen route and drops the others`() {
        switchLayer.lockedRouteNodeId = "n02"

        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(statuses).containsEntry("c01-02", OPEN).containsEntry("c01-03", DROPPED)
    }

    @Test
    fun `connection that is a route of two network switches gets the most restrictive status`() {
        switchLayer.lockedRouteNodeId = "n02"
        val otherSwitch = NetworkSwitchLayer("n02:layer-1", 1, "Network switch", "",
            mutableListOf(NetworkSwitchRoute("back", "n01"), NetworkSwitchRoute("on", "n04")), "n04")
        nodes = listOf(node("n00"), node("n01", switchLayer), node("n02", otherSwitch), node("n03"), node("n04"))

        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(statuses).containsEntry("c01-02", DROPPED).containsEntry("c02-04", OPEN)
    }

    @Test
    fun `status of a single connection matches the status calculated for the whole site`() {
        val otherSwitch = NetworkSwitchLayer("n02:layer-1", 1, "Network switch", "",
            mutableListOf(NetworkSwitchRoute("back", "n01"), NetworkSwitchRoute("on", "n04")), "n04")
        nodes = listOf(node("n00"), node("n01", switchLayer), node("n02", otherSwitch), node("n03"), node("n04"))
        val nodeById = nodes.associateBy { it.id }

        listOf(null, "n02", "n03").forEach { chosen ->
            switchLayer.lockedRouteNodeId = chosen
            val statuses = calculateConnectionStatuses(nodes, connections)
            connections.forEach { connection ->
                val single = calculateConnectionStatus(nodeById[connection.fromId]!!, nodeById[connection.toId]!!)
                assertThat(single).describedAs("${connection.id} with choice $chosen").isEqualTo(statuses[connection.id])
            }
        }
    }

    @Test
    fun `pending routes block moving but not remote scripts`() {
        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(calculateReachableNodeIds("n00", connections, statuses, pendingIsOpen = false)).containsExactlyInAnyOrder("n00", "n01")
        assertThat(calculateReachableNodeIds("n00", connections, statuses, pendingIsOpen = true))
            .containsExactlyInAnyOrder("n00", "n01", "n02", "n03", "n04")
    }

    @Test
    fun `dropped branch stays reachable when it joins the chosen branch`() {
        switchLayer.lockedRouteNodeId = "n02"
        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(calculateReachableNodeIds("n00", connections, statuses, pendingIsOpen = false))
            .containsExactlyInAnyOrder("n00", "n01", "n02", "n03", "n04")
    }

    @Test
    fun `dropped branch is unreachable when it does not join the chosen branch`() {
        switchLayer.lockedRouteNodeId = "n02"
        val treeConnections = connections.filter { it.id != "c03-04" }
        val statuses = calculateConnectionStatuses(nodes, treeConnections)

        assertThat(calculateReachableNodeIds("n00", treeConnections, statuses, pendingIsOpen = true))
            .containsExactlyInAnyOrder("n00", "n01", "n02", "n04")
    }

    @Test
    fun `isReachable starts at the start node of the site`() {
        assertThat(networkSwitchService.isReachable(SITE_ID, "n01", pendingIsOpen = false)).isTrue()
        assertThat(networkSwitchService.isReachable(SITE_ID, "n04", pendingIsOpen = false)).isFalse()
    }

    @Test
    fun `hack without a choice lists the routes`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, emptyList())

        val lines = slot<List<String>>()
        verify { connectionService.replyTerminalReceive(capture(lines)) }
        assertThat(lines.captured).hasSize(4)
        assertThat(lines.captured[1]).contains("east").contains("02")
        assertThat(lines.captured[2]).contains("west").contains("03")
        assertThat(lines.captured[3]).contains("locks after first route commit")
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `hack with a route name chooses that route and informs the site`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("EAST"))

        assertThat(switchLayer.lockedRouteNodeId).isEqualTo("n02")
        verify { nodeEntityService.save(nodes[1]) }
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_TERMINAL_RECEIVE, any()) }
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_CONNECTIONS_UPDATED, any()) }
        verify { runService.makeNeighborConnectable(nodes[1], "n02") }
    }

    @Test
    fun `hackers cut off from the start node by the choice are disconnected`() {
        every { connectionEntityService.getAll(SITE_ID) } returns connections.filter { it.id != "c03-04" }
        val onChosenBranch = hackerAt("h-east", "n02")
        val onDroppedBranch = hackerAt("h-west", "n03")
        val atSwitch = hackerAt("h-switch", "n01")
        every { hackerStateEntityService.findAllHackersInSite(SITE_ID) } returns listOf(onChosenBranch, onDroppedBranch, atSwitch)

        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("east"))

        verify(exactly = 1) { runService.hackerDisconnect(any(), any()) }
        verify { runService.hackerDisconnect(onDroppedBranch, any()) }
    }

    @Test
    fun `hackers on a dropped branch that joins the chosen branch stay connected`() {
        every { hackerStateEntityService.findAllHackersInSite(SITE_ID) } returns listOf(hackerAt("h-west", "n03"))

        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("east"))

        verify(exactly = 0) { runService.hackerDisconnect(any(), any()) }
    }

    @Test
    fun `lock changed by the GM informs the site like a hack does`() {
        switchLayer.lockedRouteNodeId = "n03"

        networkSwitchService.lockChanged(nodes[1], switchLayer)

        val receive = slot<ConnectionService.TerminalReceive>()
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_TERMINAL_RECEIVE, capture(receive)) }
        assertThat(receive.captured.lines.single()).contains("locked to [b]west").contains("Dropped routes: [ok]02")
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_CONNECTIONS_UPDATED, any()) }
        verify { runService.makeNeighborConnectable(nodes[1], "n03") }
    }

    @Test
    fun `unlock by the GM informs the site`() {
        networkSwitchService.lockChanged(nodes[1], switchLayer)

        val receive = slot<ConnectionService.TerminalReceive>()
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_TERMINAL_RECEIVE, capture(receive)) }
        assertThat(receive.captured.lines.single()).contains("unlocked. Routes back in standby.")
        verify(exactly = 0) { runService.makeNeighborConnectable(any(), any()) }
    }

    @Test
    fun `hack with a network id chooses that route`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("03"))

        assertThat(switchLayer.lockedRouteNodeId).isEqualTo("n03")
    }

    @Test
    fun `hack with an unknown route changes nothing`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("north"))

        assertThat(switchLayer.lockedRouteNodeId).isNull()
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `hack after the choice does not change it`() {
        switchLayer.lockedRouteNodeId = "n02"

        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("west"))

        assertThat(switchLayer.lockedRouteNodeId).isEqualTo("n02")
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `move is blocked until a choice is made, and dropped afterwards`() {
        assertThat(networkSwitchService.checkMove(nodes[0], nodes[1])).isNull()
        assertThat(networkSwitchService.checkMove(nodes[1], nodes[2])).contains("held in standby by a network switch")
        assertThat(networkSwitchService.checkMove(nodes[2], nodes[1])).contains("held in standby by a network switch")

        switchLayer.lockedRouteNodeId = "n02"

        assertThat(networkSwitchService.checkMove(nodes[1],nodes[2])).isNull()
        assertThat(networkSwitchService.checkMove(nodes[1],nodes[3])).contains("route down")
    }

    @Test
    fun `hacking the linked core unlocks a locked switch`() {
        switchLayer.coreLayerId = "core-node:layer-1"
        switchLayer.lockedRouteNodeId = "n02"
        every { nodeEntityService.findAllNodesLinkedToCore("core-node:layer-1") } returns listOf(nodes[1])

        val unlocked = networkSwitchService.unlockSwitchesForCore("core-node:layer-1")

        assertThat(switchLayer.lockedRouteNodeId).isNull()
        assertThat(unlocked.map { it.networkId }).containsExactly("01")
        verify { nodeEntityService.save(nodes[1]) }
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_CONNECTIONS_UPDATED, any()) }
    }

    @Test
    fun `routes whose connection was removed are removed, and the lock to such a route is cleared`() {
        switchLayer.lockedRouteNodeId = "n03"
        every { connectionEntityService.getAll(SITE_ID) } returns connections.filter { it.id != "c01-03" }

        networkSwitchService.removeRoutesWithoutConnection(SITE_ID)

        assertThat(switchLayer.routes).containsExactly(NetworkSwitchRoute("east", "n02"))
        assertThat(switchLayer.lockedRouteNodeId).isNull()
        verify { nodeEntityService.save(nodes[1]) }
    }

    @Test
    fun `routes with a connection are kept`() {
        networkSwitchService.removeRoutesWithoutConnection(SITE_ID)

        assertThat(switchLayer.routes).hasSize(2)
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `hacking the linked core leaves an unlocked switch alone`() {
        switchLayer.coreLayerId = "core-node:layer-1"
        every { nodeEntityService.findAllNodesLinkedToCore("core-node:layer-1") } returns listOf(nodes[1])

        val unlocked = networkSwitchService.unlockSwitchesForCore("core-node:layer-1")

        assertThat(unlocked).isEmpty()
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }
}
