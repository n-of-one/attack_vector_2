package org.n1.av2.backend.service.admin

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.enums.NodeType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.OsLayer
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.entity.site.layer.other.*
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.AddConnection
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.site.SiteValidationService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.asList
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class ImportService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val layoutEntityService: LayoutEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val siteValidationService: SiteValidationService,
    private val stompService: StompService,
) {

    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun import(json: String) {
        try {
            val siteName = importSite(json)
            sendResponse("Imported site: $siteName", NotyType.NEUTRAL)
        }
        catch (e: Exception) {
            sendResponse(e.message ?: "Import failed.", NotyType.ERROR)
        }
    }

    private fun sendResponse(message: String, type: NotyType) {
        val title = if (type == NotyType.ERROR) "Error" else "Success"

        val userId = (SecurityContextHolder.getContext().authentication as UserPrincipal).userId
        stompService.sendToDestination("/topic/user/${userId}", ServerActions.SERVER_NOTIFICATION, arrayOf(NotyMessage(type, title, message)))
    }

    private fun importSite(json: String): String {
        val root: JsonNode = objectMapper.readTree(json)

        val version = detectVersion(root)
        if (version != V1) error("Unsupported version: $version")

        return importV1(root)
    }

    private fun detectVersion(root: JsonNode): String {
        return root.get("exportDetails").get("version").asText()
    }

    private fun importV1(root: JsonNode): String {
        val siteProperties = importV1SiteProperties(root)
        val nodes = importV1Nodes(root)
        val connections = importV1Connections(root)

        storeLayout(siteProperties.siteId, nodes, connections)
        recreateSiteState(siteProperties.siteId)

        return siteProperties.name
    }

    private fun importV1SiteProperties(root: JsonNode): SiteProperties {
        val input: JsonNode = root.get("siteProperties")
        val name = input["name"].asText()

        val siteId = input["siteId"].asText()
        if (sitePropertiesEntityService.existsById(siteId)) {

            error("Uploading site that already exists: ${name} : $siteId")
        }

        val siteProperties = SiteProperties(
            siteId = siteId,
            name = name,
            description = input["description"].asText(),
            creator = input["creator"].asText(),
            startNodeNetworkId = input["startNodeNetworkId"].asText(),
            hackable = false,
            hackTime = "",
            shutdownEnd = null,
        )

        sitePropertiesEntityService.save(siteProperties)
        return siteProperties
    }

    private fun importV1Nodes(root: JsonNode): List<Node> {
        val v1Nodes = root.get("nodes").asList()

        val nodes = v1Nodes.map { input ->
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

        nodes.forEach { nodeEntityService.save(it) }

        return nodes
    }

    private fun storeLayout(siteId: String, nodes: List<Node>, connections: List<Connection>) {
        val layout = Layout(
            siteId = siteId,
            nodeIds = nodes.map { it.id }.toMutableList(),
            connectionIds = connections.map { it.id }.toMutableList(),
            )
        layoutEntityService.save(layout)
    }

    private fun recreateSiteState(siteId: String) {
        siteEditorStateEntityService.create(siteId)
        siteValidationService.validate(siteId)
    }


    private fun parseLayer(layersInput: List<JsonNode>): MutableList<Layer> {
        val layers = layersInput.map { layerInput -> mapV1Layer(layerInput) }
        return layers.toMutableList()
    }

    private fun mapV1Layer(input: JsonNode): Layer {
        val typeText = input["type"].asText()
        val layerType = LayerType.valueOf(typeText)
        val id = input.get("id").asText()
        val level = input.get("level").asInt()
        val name = input.get("name").asText()
        val note = input.get("note").asText()

        return when (layerType) {
            LayerType.OS -> OsLayer(type = LayerType.OS, id = id, level = level, name = name, note = note,nodeName = input.get("nodeName").asText())
            LayerType.TEXT -> TextLayer(type = LayerType.TEXT, id = id, level = level, name = name, note = note,text = input.get("text").asText())
            LayerType.CORE -> CoreLayer(type = LayerType.CORE, id = id, level = level, name = name, note = note, revealNetwork = input.get("revealNetwork").asBoolean())
            LayerType.KEYSTORE -> KeyStoreLayer(type = LayerType.KEYSTORE, id = id, level = level, name = name, note = note,iceLayerId = input.get("iceLayerId").asText())
            LayerType.STATUS_LIGHT -> mapLayerStatusLight(input, id, level, name, note)
            LayerType.LOCK -> mapLayerStatusLight(input, id, level, name, note)
            LayerType.TRIPWIRE -> mapLayerTripWire(input, id, level, name, note)
            LayerType.NETWALK_ICE -> mapIceLayer(input, layerType, id, level, name, note)
            LayerType.TANGLE_ICE -> mapIceLayer(input, layerType, id, level, name, note)
            LayerType.TAR_ICE -> mapIceLayer(input, layerType, id, level, name, note)
            LayerType.PASSWORD_ICE -> mapIceLayer(input, layerType, id, level, name, note)
            LayerType.WORD_SEARCH_ICE -> mapIceLayer(input, layerType, id, level, name, note)
        }
    }

    private fun mapLayerStatusLight(input: JsonNode, id: String, level: Int, name: String, note: String): StatusLightLayer {
        val type = LayerType.valueOf(input.get("type").asText())
        return StatusLightLayer(type = type,
            id = id, level = level, name = name, note = note,
            appId = input.get("appId").asText(),
            status = input.get("status").asBoolean(),
            textForRed = input.get("textForRed").asText(),
            textForGreen = input.get("textForGreen").asText(),
        )
    }

    private fun mapLayerTripWire(input: JsonNode, id: String, level: Int, name: String, note: String): TripwireLayer {
        return TripwireLayer( type= LayerType.TRIPWIRE,
            id = id, level = level, name = name, note = note,
            countdown = input.get("countdown").asText(),
            shutdown = input.get("shutdown").asText(),
            coreLayerId = input.get("coreLayerId").asText(),
        )
    }

    private fun mapIceLayer(input: JsonNode, layerType: LayerType, id: String, level: Int, name: String, note: String): IceLayer {
        val strength = IceStrength.valueOf(input.get("strength").asText())

        return when (layerType) {
            LayerType.NETWALK_ICE -> NetwalkIceLayer(id, LayerType.NETWALK_ICE, level, name, note, strength, false)
            LayerType.TANGLE_ICE -> TangleIceLayer(id, LayerType.TANGLE_ICE, level, name, note, strength, false)
            LayerType.TAR_ICE -> TarIceLayer(id, LayerType.TAR_ICE, level, name, note, strength, false)
            LayerType.WORD_SEARCH_ICE -> WordSearchIceLayer(id, LayerType.WORD_SEARCH_ICE, level, name, note, strength, false)
            LayerType.PASSWORD_ICE -> mapPasswordIce(input, id, level, name, note, strength)
            else -> error("Unsupported ice layer type: $layerType")
        }
    }

    private fun mapPasswordIce(input: JsonNode, id: String, level: Int, name: String, note: String, strength: IceStrength): PasswordIceLayer {
        val password = input.get("password").asText()
        val hint = input.get("hint").asText()
        return PasswordIceLayer(id, LayerType.PASSWORD_ICE, level, name, note, strength, false, password, hint)
    }

    private fun importV1Connections(root: JsonNode): List<Connection> {
        val v1connections: List<JsonNode> = root.get("connections").asList()
        val siteId = root.get("siteProperties").get("siteId").asText()
        val connections = v1connections.map { input ->
            val command = AddConnection(
                siteId = siteId,
                fromId = input.get("fromId").asText(),
                toId = input.get("toId").asText()
            )
            connectionEntityService.createConnection(command)
        }
        return connections

    }

}