package org.n1.av2.layer.other.networkswitch

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.layer.other.core.CORE_LAYER_ID
import org.n1.av2.layer.other.core.CORE_SITE_ID
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.site.entity.Connection
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.enums.NodeType
import java.time.Duration
import java.util.Optional

private const val SITE_ID = "site-1"
private const val CORE_LAYER = "n05:layer-1"
private const val TEXT_LAYER = "n05:layer-2"

/*
 Test site for validation: the network switch is in 01, connected to 00, 02 and 03. Node 04 exists but is not connected to 01.
 Node 05 has a core (layer 1) and a text layer (layer 2).
*/
class NetworkSwitchLayerTest {

    private lateinit var switchLayer: NetworkSwitchLayer
    private lateinit var validationContext: ValidationContext
    private val nodeRepo = mockk<NodeRepo>()

    @BeforeEach
    fun setup() {
        switchLayer = NetworkSwitchLayer(
            "n01:layer-1", 1, "Network switch", "",
            mutableListOf(NetworkSwitchRoute("east", "n02"), NetworkSwitchRoute("west", "n03")), null
        )

        val switchNode = node("n01", switchLayer)
        val coreNode = node("n05", CoreLayer(CORE_LAYER, 1, "Core"), TextLayer(TEXT_LAYER, 2, "Database"))
        val nodes = listOf(node("n00"), switchNode, node("n02"), node("n03"), node("n04"), coreNode)
        val connections = listOf(
            Connection("c00-01", SITE_ID, "n00", "n01"),
            Connection("c01-02", SITE_ID, "n01", "n02"),
            Connection("c03-01", SITE_ID, "n03", "n01"), // reversed direction, also counts as connected
            Connection("c00-05", SITE_ID, "n00", "n05"),
        )
        every { nodeRepo.findById(any()) } answers { Optional.ofNullable(nodes.find { it.id == firstArg() }) }

        val siteProperties = SiteProperties(siteId = SITE_ID, name = "test", ownerUserId = "owner")
        validationContext = ValidationContext(switchNode, nodes, connections, siteProperties, mockk(), nodeRepo, Duration.ZERO)
    }

    private fun node(id: String, vararg layers: Layer) =
        Node(id, SITE_ID, NodeType.TRANSIT_2, 0, 0, layers.toMutableList(), id.removePrefix("n"))

    private fun validationErrors(): List<String> {
        return switchLayer.allValidationMethods().mapNotNull { validate ->
            try {
                validate(validationContext)
                null
            } catch (exception: SiteValidationException) {
                exception.message
            }
        }
    }

    @Test
    fun `valid network switch has no validation errors`() {
        assertThat(validationErrors()).isEmpty()
    }

    @Test
    fun `network switch needs at least 2 routes`() {
        switchLayer.routes.removeAt(1)

        assertThat(validationErrors()).containsExactly("Network switch needs at least 2 routes.")
    }

    @Test
    fun `switched route without a name is an error that shows the network id`() {
        switchLayer.update(ROUTE_NAME_FOR + "n03", "")

        assertThat(validationErrors()).containsExactly("Network switch route → 03 needs a route name.")
    }

    @Test
    fun `two routes without a name are not reported as a duplicate name`() {
        switchLayer.update(ROUTE_NAME_FOR + "n02", "")
        switchLayer.update(ROUTE_NAME_FOR + "n03", "")

        assertThat(validationErrors()).containsExactly("Network switch route → 02 needs a route name.")
    }

    @Test
    fun `route name can only contain letters, digits and some punctuation`() {
        switchLayer.update(ROUTE_NAME_FOR + "n02", "north east")

        assertThat(validationErrors()).containsExactly(
            "Network switch route name 'north east' can only contain letters, digits, '.', '-' and '_'."
        )
    }

    @Test
    fun `route name with allowed punctuation is valid`() {
        switchLayer.update(ROUTE_NAME_FOR + "n02", "R-2.north_east")

        assertThat(validationErrors()).isEmpty()
    }

    @Test
    fun `route to a node that is not connected is an error`() {
        switchLayer.update(ROUTE_NAME_FOR + "n04", "far")

        assertThat(validationErrors()).containsExactly("Network switch route 'far' leads to a node that is not connected to this node.")
    }

    @Test
    fun `route names must be unique, ignoring case`() {
        switchLayer.update(ROUTE_NAME_FOR + "n03", "East")

        assertThat(validationErrors()).containsExactly("Network switch has more than one route named 'east'.")
    }

    @Test
    fun `link to an existing core is valid`() {
        switchLayer.update(CORE_LAYER_ID, CORE_LAYER)

        assertThat(validationErrors()).isEmpty()
    }

    @Test
    fun `link to a core in a removed node is an error`() {
        switchLayer.update(CORE_LAYER_ID, "n99:layer-1")

        assertThat(validationErrors()).containsExactly("Network switch connected to core layer that was later removed.")
    }

    @Test
    fun `link to a layer that is not a core is an error`() {
        switchLayer.update(CORE_LAYER_ID, TEXT_LAYER)

        assertThat(validationErrors()).containsExactly("Network switch connected to a layer that is not a core.")
    }

    @Test
    fun `link to a core layer that was removed from an existing node is an error`() {
        switchLayer.update(CORE_LAYER_ID, "n05:layer-9")

        assertThat(validationErrors()).containsExactly("Network switch connected to a layer that is not a core.")
    }

    @Test
    fun `switching off the chosen route clears the choice`() {
        switchLayer.lockedRouteNodeId = "n02"

        switchLayer.update(ROUTE_SWITCHED_FOR + "n02", "false")

        assertThat(switchLayer.routes.map { it.name }).containsExactly("west")
        assertThat(switchLayer.lockedRouteNodeId).isNull()
    }

    @Test
    fun `switching on a route adds it without a name, clearing the name keeps it`() {
        switchLayer.update(ROUTE_SWITCHED_FOR + "n00", "true")
        switchLayer.update(ROUTE_NAME_FOR + "n02", "")

        assertThat(switchLayer.routes).containsExactly(
            NetworkSwitchRoute("", "n02"), NetworkSwitchRoute("west", "n03"), NetworkSwitchRoute("", "n00")
        )
    }

    @Test
    fun `naming a route that is not switched switches it`() {
        switchLayer.update(ROUTE_NAME_FOR + "n00", "back")

        assertThat(switchLayer.routeFor("n00")).isEqualTo(NetworkSwitchRoute("back", "n00"))
    }

    @Test
    fun `copy does not copy the choice`() {
        switchLayer.lockedRouteNodeId = "n02"

        val copy = NetworkSwitchLayer("other:layer-1", switchLayer)

        assertThat(copy.routes).isEqualTo(switchLayer.routes)
        assertThat(copy.lockedRouteNodeId).isNull()
    }

    @Test
    fun `copy keeps the core link`() {
        switchLayer.coreLayerId = "core-node:layer-1"
        switchLayer.coreSiteId = "other-site"

        val copy = NetworkSwitchLayer("other:layer-1", switchLayer)

        assertThat(copy.coreLayerId).isEqualTo("core-node:layer-1")
        assertThat(copy.coreSiteId).isEqualTo("other-site")
    }

    @Test
    fun `choosing another core site clears the core layer`() {
        switchLayer.update(CORE_LAYER_ID, "core-node:layer-1")
        assertThat(switchLayer.coreLayerId).isEqualTo("core-node:layer-1")

        switchLayer.update(CORE_SITE_ID, "other-site")

        assertThat(switchLayer.coreSiteId).isEqualTo("other-site")
        assertThat(switchLayer.coreLayerId).isNull()
    }
}
