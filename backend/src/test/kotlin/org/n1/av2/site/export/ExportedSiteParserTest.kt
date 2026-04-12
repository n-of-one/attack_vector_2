package org.n1.av2.site.export

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjusterLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjustmentRecurring
import org.n1.av2.layer.other.timeradjuster.TimerAdjustmentType
import org.n1.av2.site.entity.enums.LayerType

class ExportedSiteParserTest {

    private val parser = ExportedSiteParser()

    private fun loadJson(resourceName: String): String {
        return this::class.java.getResource(resourceName)?.readText()
            ?: error("Test resource not found: $resourceName")
    }

    private fun parseLayer(resourceName: String) = parser.parse(loadJson(resourceName)).nodes[0].layers

    @Test
    fun `fixOsLayerId converts old dash format to colon format`() {
        val layers = parseLayer("/export/os-old-id-format.json")

        assertEquals("n1:layer-0000", (layers[0] as OsLayer).id)
    }

    @Test
    fun `fixOsLayerId keeps colon format unchanged`() {
        val layers = parseLayer("/export/os-new-id-format.json")

        assertEquals("n1:layer-0000", (layers[0] as OsLayer).id)
    }

    @Test
    fun `mapLayerStatusLight parses V12 boolean status format`() {
        val layer = parseLayer("/export/status-light-v12.json")[1] as StatusLightLayer

        assertEquals(1, layer.currentOption)
        assertEquals(2, layer.options.size)
        assertEquals("Locked", layer.options[0].text)
        assertEquals("red", layer.options[0].color)
        assertEquals("Unlocked", layer.options[1].text)
        assertEquals("lime", layer.options[1].color)
        assertEquals("Switch", layer.switchLabel)
    }

    @Test
    fun `mapLayerStatusLight parses options format`() {
        val layer = parseLayer("/export/status-light-options.json")[1] as StatusLightLayer

        assertEquals(2, layer.currentOption)
        assertEquals(3, layer.options.size)
        assertEquals("Red", layer.options[0].text)
        assertEquals("Green", layer.options[2].text)
        assertEquals("lime", layer.options[2].color)
        assertEquals("Change", layer.switchLabel)
    }

    @Test
    fun `mapTimerAdjusterLayer uses increase field fallback and applies defaults`() {
        val layer = parseLayer("/export/timer-adjuster-old-fields.json")[1] as TimerAdjusterLayer

        assertEquals("3:00", layer.amount)
        assertEquals(TimerAdjustmentType.SPEED_UP, layer.adjustmentType)
        assertEquals(TimerAdjustmentRecurring.EVERY_ENTRY, layer.recurring)
    }

    @Test
    fun `mapTimerAdjusterLayer parses all explicit fields`() {
        val layer = parseLayer("/export/timer-adjuster-all-fields.json")[1] as TimerAdjusterLayer

        assertEquals("5:00", layer.amount)
        assertEquals(TimerAdjustmentType.SLOW_DOWN, layer.adjustmentType)
        assertEquals(TimerAdjustmentRecurring.EACH_HACKER_ONCE, layer.recurring)
    }

    @Test
    fun `SHUTDOWN_ACCELERATOR type maps to TimerAdjusterLayer`() {
        val layer = parseLayer("/export/timer-adjuster-as-shutdown-accelerator.json")[1] as TimerAdjusterLayer

        assertEquals(LayerType.TIMER_ADJUSTER, layer.type)
        assertEquals("2:00", layer.amount)
        assertEquals(TimerAdjustmentType.SPEED_UP, layer.adjustmentType)
        assertEquals(TimerAdjustmentRecurring.EVERY_ENTRY, layer.recurring)

    }

    @Test
    fun `siteProperties uses creator as purpose fallback`() {
        val blueprint = parser.parse(loadJson("/export/site-purpose-creator-fallback.json"))

        assertEquals("gm", blueprint.siteProperties.purpose)
    }
}
