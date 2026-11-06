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
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.OPEN
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.PENDING
import org.n1.av2.layer.other.networkswitch.ConnectionStatus.SEVERED
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
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
 Test site, 00 is the start node, 01 has the network switch with paths east (02) and west (03).
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
            mutableListOf(NetworkSwitchPath("east", "n02"), NetworkSwitchPath("west", "n03")), null
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
    }

    private fun node(id: String, vararg layers: NetworkSwitchLayer) =
        Node(id, SITE_ID, NodeType.TRANSIT_2, 0, 0, layers.toMutableList(), id.removePrefix("n"))

    private fun connection(id: String, fromId: String, toId: String) = Connection(id, SITE_ID, fromId, toId)

    @Test
    fun `paths are pending until a choice is made`() {
        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(statuses).containsEntry("c00-01", OPEN)
            .containsEntry("c01-02", PENDING)
            .containsEntry("c01-03", PENDING)
            .containsEntry("c02-04", OPEN)
    }

    @Test
    fun `choice opens the chosen path and severs the others`() {
        switchLayer.chosenNodeId = "n02"

        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(statuses).containsEntry("c01-02", OPEN).containsEntry("c01-03", SEVERED)
    }

    @Test
    fun `connection that is a path of two network switches gets the most restrictive status`() {
        switchLayer.chosenNodeId = "n02"
        val otherSwitch = NetworkSwitchLayer("n02:layer-1", 1, "Network switch", "",
            mutableListOf(NetworkSwitchPath("back", "n01"), NetworkSwitchPath("on", "n04")), "n04")
        nodes = listOf(node("n00"), node("n01", switchLayer), node("n02", otherSwitch), node("n03"), node("n04"))

        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(statuses).containsEntry("c01-02", SEVERED).containsEntry("c02-04", OPEN)
    }

    @Test
    fun `pending paths block moving but not remote scripts`() {
        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(calculateReachableNodeIds("n00", connections, statuses, pendingIsOpen = false)).containsExactlyInAnyOrder("n00", "n01")
        assertThat(calculateReachableNodeIds("n00", connections, statuses, pendingIsOpen = true))
            .containsExactlyInAnyOrder("n00", "n01", "n02", "n03", "n04")
    }

    @Test
    fun `severed branch stays reachable when it joins the chosen branch`() {
        switchLayer.chosenNodeId = "n02"
        val statuses = calculateConnectionStatuses(nodes, connections)

        assertThat(calculateReachableNodeIds("n00", connections, statuses, pendingIsOpen = false))
            .containsExactlyInAnyOrder("n00", "n01", "n02", "n03", "n04")
    }

    @Test
    fun `severed branch is unreachable when it does not join the chosen branch`() {
        switchLayer.chosenNodeId = "n02"
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
    fun `hack without a choice lists the paths`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, emptyList())

        val lines = slot<List<String>>()
        verify { connectionService.replyTerminalReceive(capture(lines)) }
        assertThat(lines.captured).hasSize(3)
        assertThat(lines.captured[1]).contains("east").contains("02")
        assertThat(lines.captured[2]).contains("west").contains("03")
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `hack with a label chooses that path and informs the site`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("EAST"))

        assertThat(switchLayer.chosenNodeId).isEqualTo("n02")
        verify { nodeEntityService.save(nodes[1]) }
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_TERMINAL_RECEIVE, any()) }
        verify { connectionService.toSite(SITE_ID, ServerActions.SERVER_CONNECTIONS_UPDATED, any()) }
    }

    @Test
    fun `hack with a network id chooses that path`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("03"))

        assertThat(switchLayer.chosenNodeId).isEqualTo("n03")
    }

    @Test
    fun `hack with an unknown path changes nothing`() {
        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("north"))

        assertThat(switchLayer.chosenNodeId).isNull()
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `hack after the choice does not change it`() {
        switchLayer.chosenNodeId = "n02"

        networkSwitchService.hack(SWITCH_LAYER_ID, listOf("west"))

        assertThat(switchLayer.chosenNodeId).isEqualTo("n02")
        verify(exactly = 0) { nodeEntityService.save(any()) }
    }

    @Test
    fun `move is blocked until a choice is made, and severed afterwards`() {
        assertThat(networkSwitchService.checkMove("n00", nodes[1])).isNull()
        assertThat(networkSwitchService.checkMove("n01", nodes[2])).contains("choose a path first").contains("hack [primary]1")
        assertThat(networkSwitchService.checkMove("n02", nodes[1])).contains("other side")

        switchLayer.chosenNodeId = "n02"

        assertThat(networkSwitchService.checkMove("n01", nodes[2])).isNull()
        assertThat(networkSwitchService.checkMove("n01", nodes[3])).contains("severed")
    }

    @Test
    fun `removing the label of the chosen path clears the choice`() {
        switchLayer.chosenNodeId = "n02"

        switchLayer.update(PATH_LABEL_FOR + "n02", "")

        assertThat(switchLayer.paths.map { it.label }).containsExactly("west")
        assertThat(switchLayer.chosenNodeId).isNull()
    }

    @Test
    fun `copy does not copy the choice`() {
        switchLayer.chosenNodeId = "n02"

        val copy = NetworkSwitchLayer("other:layer-1", switchLayer)

        assertThat(copy.paths).isEqualTo(switchLayer.paths)
        assertThat(copy.chosenNodeId).isNull()
    }
}
