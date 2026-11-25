package org.n1.av2.layer.other.uplink

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.n1.av2.editor.SiteStateMessageType
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesRepo
import org.n1.av2.site.entity.enums.NodeType
import java.time.Duration

private const val SITE_ID = "site-a"

class UplinkLayerTest {

    private lateinit var uplink: UplinkLayer
    private lateinit var validationContext: ValidationContext
    private val sitePropertiesRepo = mockk<SitePropertiesRepo>()
    private val sites = listOf(
        SiteProperties(siteId = SITE_ID, name = "a", ownerUserId = "owner"),
        SiteProperties(siteId = "site-internal", name = "internal", ownerUserId = "owner", internal = true),
        SiteProperties(siteId = "site-external", name = "external", ownerUserId = "owner"),
    )

    @BeforeEach
    fun setup() {
        uplink = UplinkLayer("n01:layer-1", 1, "Uplink")
        val node = Node("n01", SITE_ID, NodeType.TRANSIT_2, 0, 0, mutableListOf(uplink), "01")
        every { sitePropertiesRepo.findBySiteId(any()) } answers { sites.find { it.siteId == firstArg() } }
        validationContext = ValidationContext(node, listOf(node), emptyList(), sites[0], sitePropertiesRepo, mockk(), Duration.ZERO)
    }

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
        uplink.update(TARGET_SITE_ID, "site-internal")
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
        uplink.update(TARGET_SITE_ID, SITE_ID)
        assertThat(validationMessages()).containsExactly(SiteStateMessageType.ERROR to "Uplink cannot target its own site.")
    }

    @Test
    fun `external target is only info`() {
        uplink.update(TARGET_SITE_ID, "site-external")
        assertThat(validationMessages())
            .containsExactly(SiteStateMessageType.INFO to "Uplink target site 'external' is not internal, it can also be reached from outside.")
    }

    @Test
    fun `blank target clears it`() {
        uplink.update(TARGET_SITE_ID, "site-internal")
        uplink.update(TARGET_SITE_ID, "")
        assertThat(uplink.targetSiteId).isNull()
    }
}
