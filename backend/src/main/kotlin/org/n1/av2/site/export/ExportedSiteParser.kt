package org.n1.av2.site.export

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.app.status_light.StatusLightOption
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.netwalk.NetwalkIceLayer
import org.n1.av2.layer.ice.password.PasswordIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.tangle.TangleIceLayer
import org.n1.av2.layer.ice.tar.TarIceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchIceLayer
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.script.ScriptCreditsLayer
import org.n1.av2.layer.other.script.ScriptInteractionLayer
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjusterLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjustmentRecurring
import org.n1.av2.layer.other.timeradjuster.TimerAdjustmentType
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.util.asList
import org.n1.av2.site.entity.Connection
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.site.entity.enums.NodeType


fun JsonNode.getAsTextOrNull(field: String): String? {
    return if (this.has(field) && !this.get(field).isNull) this.get(field).asText() else null
}

fun JsonNode.getAsIntOrNull(field: String): Int? {
    return if (this.has(field) && !this.get(field).isNull) this.get(field).asInt() else null
}


data class SiteBlueprint(
    val siteProperties: SiteProperties,
    val nodes: List<Node>,
    val connections: List<Connection>,
)


class ExportedSiteParser {

    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun parse(json: String): SiteBlueprint {
        val root: JsonNode = objectMapper.readTree(json)

        val version = detectVersion(root)
        if (version != V1 && version != V2) error("Unsupported version: $version")

        val siteProperties = parseSiteProperties(root)
        val nodes = parseNodes(root)
        val connections = parseConnections(root)

        return SiteBlueprint(siteProperties, nodes, connections)
    }

    private fun detectVersion(root: JsonNode): String {
        return root.get("exportDetails").get("version").asText()
    }

    private fun parseSiteProperties(root: JsonNode): SiteProperties {
        val input: JsonNode = root.get("siteProperties")

        return SiteProperties(
            siteId = input["siteId"].asText(),
            name = input["name"].asText(),
            description = input["description"].asText(),
            purpose = input["purpose"]?.asText() ?: input["creator"].asText(),
            ownerUserId = "",
            startNodeNetworkId = input["startNodeNetworkId"].asText(),
            hackable = false,
            shutdownEnd = null,
            nodesLocked = input["nodesLocked"]?.asBoolean() == true,
        )
    }

    private fun parseNodes(root: JsonNode): List<Node> {
        val v1Nodes = root.get("nodes").asList()

        return v1Nodes.map { input ->
            Node(
                id = input["id"].asText(),
                siteId = input["siteId"].asText(),
                type = NodeType.valueOf(input["type"].asText()),
                x = input["x"].asInt(),
                y = input["y"].asInt(),
                layers = parseLayer(input.get("layers").asList()),
                networkId = input["networkId"].asText(),
            )
        }
    }

    private fun parseConnections(root: JsonNode): List<Connection> {
        val v1connections: List<JsonNode> = root.get("connections").asList()
        val siteId = root.get("siteProperties").get("siteId").asText()
        return v1connections.map { input ->
            Connection(
                id = "import-${input.get("fromId").asText()}-${input.get("toId").asText()}",
                siteId = siteId,
                fromId = input.get("fromId").asText(),
                toId = input.get("toId").asText()
            )
        }
    }

    private fun parseLayer(layersInput: List<JsonNode>): MutableList<Layer> {
        val layers = layersInput.map { layerInput -> mapV1Layer(layerInput) }
        return layers.toMutableList()
    }

    private fun mapV1Layer(input: JsonNode): Layer {
        val typeText = input["type"].asText()
        val id = input.get("id").asText()
        val level = input.get("level").asInt()
        val name = input.get("name").asText()
        val note = input.get("note").asText()

        try {
            return when (val layerType = LayerType.valueOf(typeText)) {
                LayerType.OS -> mapLayerOs(input, id, level, name, note)
                LayerType.TEXT -> TextLayer(id = id, level = level, name = name, note = note, text = input.get("text").asText())
                LayerType.CORE -> mapLayerCore(input, id, level, name, note)
                LayerType.KEYSTORE -> mapLayerKeystore(input, id, level, name, note)
                LayerType.STATUS_LIGHT -> mapLayerStatusLight(input, id, level, name, note)
                LayerType.LOCK -> mapLayerStatusLight(input, id, level, name, note)
                LayerType.TRIPWIRE -> mapLayerTripWire(input, id, level, name, note)
                LayerType.TIMER_ADJUSTER -> mapTimerAdjusterLayer(input, id, level, name, note)
                LayerType.SCRIPT_INTERACTION -> mapLayerScriptInteraction(input, id, level, name, note)
                LayerType.SCRIPT_CREDITS -> mapLayerScriptCredits(input, id, level, name, note)
                LayerType.NETWALK_ICE,
                LayerType.TANGLE_ICE,
                LayerType.TAR_ICE,
                LayerType.PASSWORD_ICE,
                LayerType.WORD_SEARCH_ICE,
                LayerType.SWEEPER_ICE -> mapIceLayer(input, layerType, id, level, name, note)
            }
        } catch (_: IllegalArgumentException) {
            if (typeText == "SHUTDOWN_ACCELERATOR") {
                return mapTimerAdjusterLayer(input, id, level, name, note)
            }
            error("Unsupported layer type: $typeText")
        }
    }

    private fun mapLayerCore(input: JsonNode, id: String, level: Int, name: String, note: String): CoreLayer {
        return CoreLayer(
            id = id,
            level = level,
            name = name,
            note = note,
            revealNetwork = input.get("revealNetwork").asBoolean()
        )
    }

    private fun mapLayerOs(input: JsonNode, id: String, level: Int, name: String, note: String): OsLayer {
        val nodeName = input.get("nodeName").asText()
        val layerId = fixOsLayerId(id)
        return OsLayer(id = layerId, level = level, name = name, note = note, nodeName = nodeName)
    }

    private fun mapLayerKeystore(input: JsonNode, id: String, level: Int, name: String, note: String): KeyStoreLayer {
        return KeyStoreLayer(
            id = id,
            level = level,
            name = name,
            note = note,
            iceLayerId = input.get("iceLayerId").asText()
        )
    }

    // Initial version had OS layerId: {nodeId}-layer-0000.
    private fun fixOsLayerId(idInput: String): String {
        if (!(idInput.endsWith("-layer-0000"))) return idInput

        val nodeId = idInput.substringBefore("-layer-0000")
        return "${nodeId}:layer-0000"
    }

    private fun mapLayerStatusLight(input: JsonNode, id: String, level: Int, name: String, note: String): StatusLightLayer {
        val type = LayerType.valueOf(input.get("type").asText())

        if (input.has("status")) {
            // V12 and earlier: 2-options switch
            val status = input.get("status").asBoolean()
            val textForRed = input.get("textForRed").asText()
            val textForGreen = input.get("textForGreen").asText()
            val switchLabel = "Switch"

            val statusLight = StatusLightLayer(id, type, level, name, switchLabel, textForRed, textForGreen)
            statusLight.currentOption = if (status) 1 else 0
            return statusLight
        }

        val optionElement = input.get("options").asList()
        val options = optionElement.map { optionNode ->
            StatusLightOption(
                text = optionNode.get("text").asText(),
                color = optionNode.get("color").asText()
            )
        }.toMutableList()

        return StatusLightLayer(
            type = type,
            id = id, level = level, name = name, note = note,
            switchLabel = input.get("switchLabel").asText(),
            currentOption = input.get("currentOption").asInt(),
            options = options
        )
    }

    private fun mapLayerTripWire(input: JsonNode, id: String, level: Int, name: String, note: String): TripwireLayer {
        return TripwireLayer(
            id = id, level = level, name = name, note = note,
            countdown = input.get("countdown").asText(),
            shutdown = input.get("shutdown").asText(),
            coreLayerId = input.getAsTextOrNull("coreLayerId"),
            coreSiteId = input.getAsTextOrNull("coreSiteId"),
            coreSiteName = input.getAsTextOrNull("coreSiteName"),
            coreNetworkId = input.getAsTextOrNull("coreNetworkId"),
            coreLayerLevel = input.getAsIntOrNull("coreLayerLevel"),
        )
    }

    private fun mapTimerAdjusterLayer(input: JsonNode, id: String, level: Int, name: String, note: String): TimerAdjusterLayer {
        val amount = input.get("amount")?.asText() ?: input.get("increase").asText() // In initial version the field was called "increase"
        val adjustmentType = if (input.has("adjustmentType")) { TimerAdjustmentType.valueOf(input.get("adjustmentType").asText()) } else { TimerAdjustmentType.SPEED_UP }
        val recurring = if (input.has("recurring")) { TimerAdjustmentRecurring.valueOf(input.get("recurring").asText()) } else { TimerAdjustmentRecurring.EVERY_ENTRY }

        return TimerAdjusterLayer(
            id = id, level = level, name = name, note = note,
            amount = amount, adjustmentType = adjustmentType, recurring = recurring,
        )
    }

    private fun mapLayerScriptInteraction(input: JsonNode, id: String, level: Int, name: String, note: String): ScriptInteractionLayer {
        return ScriptInteractionLayer(
            id = id, level = level, name = name, note = note,
            interactionKey = input.get("interactionKey").asText(),
            message = input.get("message").asText(),
        )
    }

    private fun mapLayerScriptCredits(input: JsonNode, id: String, level: Int, name: String, note: String): ScriptCreditsLayer {
        return ScriptCreditsLayer(
            id = id, level = level, name = name, note = note,
            amount = input.get("amount").asInt(),
            stolen = input.get("stolen").asBoolean(),
        )
    }

    private fun mapIceLayer(input: JsonNode, layerType: LayerType, id: String, level: Int, name: String, note: String): IceLayer {
        val strength = IceStrength.valueOf(input.get("strength").asText())

        return when (layerType) {
            LayerType.NETWALK_ICE -> NetwalkIceLayer(id, level, name, note, strength, false, null)
            LayerType.TANGLE_ICE -> TangleIceLayer(id, level, name, note, strength, false, null)
            LayerType.TAR_ICE -> TarIceLayer(id, level, name, note, strength, false)
            LayerType.WORD_SEARCH_ICE -> WordSearchIceLayer(id, level, name, note, strength, false, null)
            LayerType.PASSWORD_ICE -> mapPasswordIce(input, id, level, name, note, strength)
            LayerType.SWEEPER_ICE -> SweeperIceLayer(id, level, name, note, strength, false, null)
            else -> error("Unsupported ice layer type: $layerType")
        }
    }

    private fun mapPasswordIce(input: JsonNode, id: String, level: Int, name: String, note: String, strength: IceStrength): PasswordIceLayer {
        val password = input.get("password").asText()
        val hint = input.get("hint").asText()
        return PasswordIceLayer(id, level, name, note, strength, false, password, hint)
    }
}
