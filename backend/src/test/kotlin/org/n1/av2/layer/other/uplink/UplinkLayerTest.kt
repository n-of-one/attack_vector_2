package org.n1.av2.layer.other.uplink

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.n1.av2.editor.SiteStateMessageType
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.SiteValidationService
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.LayerEditContext
import org.n1.av2.layer.LayerEditServices
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeRepo
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.entity.SitePropertiesRepo
import org.n1.av2.site.entity.enums.NodeType
import java.time.Duration

private const val SITE_ID = "site-a"

class UplinkLayerTest {

    private lateinit var uplink: UplinkLayer
    private lateinit var node: Node
    private lateinit var validationContext: ValidationContext
    private val sitePropertiesRepo = mockk<SitePropertiesRepo>()
    private val nodeRepo = mockk<NodeRepo>()
    private val sitePropertiesEntityService = mockk<SitePropertiesEntityService>()
    private val siteValidationService = mockk<SiteValidationService>(relaxed = true)
    private val layerEditServices = LayerEditServices(mockk(), sitePropertiesEntityService, siteValidationService, mockk(), mockk())
    private val sites = listOf(
        SiteProperties(siteId = SITE_ID, name = "a", ownerUserId = "owner"),
        SiteProperties(siteId = "site-internal", name = "internal", ownerUserId = "owner", internal = true),
        SiteProperties(siteId = "site-external", name = "external", ownerUserId = "owner"),
    )

    @BeforeEach
    fun setup() {
        uplink = UplinkLayer("n01:layer-1", 1, "Uplink")
        node = Node("n01", SITE_ID, NodeType.TRANSIT_2, 0, 0, mutableListOf(uplink), "01")
        every { sitePropertiesRepo.findBySiteId(any()) } answers { sites.find { it.siteId == firstArg() } }
        every { sitePropertiesEntityService.getBySiteId(any()) } answers { sites.first { it.siteId == firstArg() } }
        every { nodeRepo.findBySiteIdAndNetworkId(any(), any()) } returns null
        every { nodeRepo.findBySiteIdAndNetworkId("site-external", "05") } returns
            Node("e05", "site-external", NodeType.TRANSIT_2, 0, 0, mutableListOf(), "05")
        validationContext = ValidationContext(node, listOf(node), emptyList(), sites[0], sitePropertiesRepo, nodeRepo, Duration.ZERO)
    }

    private fun editContext() = LayerEditContext(SITE_ID, node, layerEditServices)

    private fun update(key: String, value: String) = uplink.update(key, value, editContext())

    private fun validationMessages(): List<Pair<SiteStateMessageType, String>> {
        return uplink.allValidationMethods().mapNotNull { validate ->
            try {
                validate(validationContext)
                null
            } catch (exception: SiteValidationException) {
                exception.type to exception.message!!
            }
        }
    }

    @Test
    fun `internal target is valid`() {
        update(TARGET_SITE_ID, "site-internal")
        assertThat(validationMessages()).isEmpty()
    }

    @Test
    fun `no target is an error`() {
        assertThat(validationMessages()).containsExactly(SiteStateMessageType.ERROR to "Uplink has no target site.")
    }

    @Test
    fun `missing target site is an error`() {
        uplink.targetSiteId = "site-removed"
        uplink.targetSiteName = "removed"
        assertThat(validationMessages()).containsExactly(SiteStateMessageType.ERROR to "Uplink target site 'removed' does not exist.")
    }

    @Test
    fun `own site as target is an error`() {
        update(TARGET_SITE_ID, SITE_ID)
        assertThat(validationMessages()).containsExactly(SiteStateMessageType.ERROR to "Uplink cannot target its own site.")
    }

    @Test
    fun `external target is only info`() {
        update(TARGET_SITE_ID, "site-external")
        assertThat(validationMessages())
            .containsExactly(SiteStateMessageType.INFO to "Uplink target site 'external' is not internal, it can also be reached from outside.")
    }

    @Test
    fun `target node in an external site is valid, without info`() {
        update(TARGET_SITE_ID, "site-external")
        update(TARGET_NODE_NETWORK_ID, "05")
        assertThat(validationMessages()).isEmpty()
    }

    @Test
    fun `missing target node is an error`() {
        update(TARGET_SITE_ID, "site-external")
        update(TARGET_NODE_NETWORK_ID, "06")
        assertThat(validationMessages()).containsExactly(SiteStateMessageType.ERROR to "Uplink target node '06' does not exist in site 'external'.")
    }

    @Test
    fun `blank target node means the start node`() {
        update(TARGET_NODE_NETWORK_ID, " 05 ")
        assertThat(uplink.targetNodeNetworkId).isEqualTo("05")
        update(TARGET_NODE_NETWORK_ID, " ")
        assertThat(uplink.targetNodeNetworkId).isNull()
    }

    @Test
    fun `blank target clears it`() {
        update(TARGET_SITE_ID, "site-internal")
        update(TARGET_SITE_ID, "")
        assertThat(uplink.targetSiteId).isNull()
        assertThat(uplink.targetSiteName).isNull()
    }

    @Test
    fun `setting the target looks up its name`() {
        update(TARGET_SITE_ID, "site-internal")
        assertThat(uplink.targetSiteName).isEqualTo("internal")
    }

    @Test
    fun `changing the target validates the previous and the new target site after saving`() {
        uplink.targetSiteId = "site-external"
        val context = editContext()

        uplink.update(TARGET_SITE_ID, "site-internal", context)
        verify(exactly = 0) { siteValidationService.validate(any<String>()) }

        context.runAfterSave()
        verify { siteValidationService.validate("site-external") }
        verify { siteValidationService.validate("site-internal") }
    }

    @Test
    fun `removing the uplink validates its target site after saving`() {
        uplink.targetSiteId = "site-internal"
        val context = editContext()

        uplink.onRemove(context)
        context.runAfterSave()

        verify { siteValidationService.validate("site-internal") }
    }
}
