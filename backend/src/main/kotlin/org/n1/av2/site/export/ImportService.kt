package org.n1.av2.site.export

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.n1.av2.editor.AddConnection
import org.n1.av2.editor.SiteEditorStateEntityService
import org.n1.av2.editor.SiteValidationService
import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.frontend.model.NotyType
import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightLayer
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
import org.n1.av2.layer.other.script.ScriptInteractionLayer
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.util.asList
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.*
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.site.entity.enums.NodeType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class ImportService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val siteValidationService: SiteValidationService,
    private val connectionService: ConnectionService,
    private val currentUserService: CurrentUserService,
    private val siteService: SiteService,
) {

    private val logger = mu.KotlinLogging.logger {}

    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


    fun import(json: String) {
        try {
            val siteName = importSite(json)
            sendResponse("Imported site", siteName, NotyType.NEUTRAL)
            siteService.sendSitesList()
        } catch (e: Exception) {
            logger.error(e.message, e)
            sendResponse("Error", e.message ?: "Import failed.", NotyType.ERROR)
        }
    }

    private fun sendResponse(title: String, message: String, type: NotyType) {
        val userId = (SecurityContextHolder.getContext().authentication as UserPrincipal).userId
        connectionService.sendToDestination("/topic/user/${userId}", ServerActions.SERVER_NOTIFICATION, arrayOf(NotyMessage(type, title, message)))
    }

    fun importSite(json: String): String {
        val root: JsonNode = objectMapper.readTree(json)

        val version = detectVersion(root)
        if (version != V1 && version != V2) error("Unsupported version: $version")

        return importV1orV2(root)
    }

    private fun detectVersion(root: JsonNode): String {
        return root.get("exportDetails").get("version").asText()
    }

    private fun importV1orV2(root: JsonNode): String {
        val siteProperties = importSiteProperties(root)
        importNodes(root)
        importConnections(root)
        recreateSiteState(siteProperties.siteId)

        return siteProperties.name
    }

    private fun importSiteProperties(root: JsonNode): SiteProperties {
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
            purpose = input["purpose"]?.asText() ?: input["creator"].asText(),
            ownerUserId = currentUserService.userEntity.id,
            startNodeNetworkId = input["startNodeNetworkId"].asText(),
            hackable = false,
            shutdownEnd = null,
        )

        sitePropertiesEntityService.save(siteProperties)
        return siteProperties
    }

    private fun importNodes(root: JsonNode): List<Node> {
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
            LayerType.OS -> mapLayerOs(input, id, level, name, note)
            LayerType.TEXT -> TextLayer(type = LayerType.TEXT, id = id, level = level, name = name, note = note, text = input.get("text").asText())
            LayerType.CORE -> mapLayerCore(input, id, level, name, note)
            LayerType.KEYSTORE -> mapLayerKeystore(input, id, level, name, note)
            LayerType.STATUS_LIGHT -> mapLayerStatusLight(input, id, level, name, note)
            LayerType.LOCK -> mapLayerStatusLight(input, id, level, name, note)
            LayerType.TRIPWIRE -> mapLayerTripWire(input, id, level, name, note)
            LayerType.SCRIPT_INTERACTION -> mapLayerScriptInteraction(input, id, level, name, note)
            LayerType.NETWALK_ICE,
            LayerType.TANGLE_ICE,
            LayerType.TAR_ICE,
            LayerType.PASSWORD_ICE,
            LayerType.WORD_SEARCH_ICE,
            LayerType.SWEEPER_ICE -> mapIceLayer(input, layerType, id, level, name, note)
        }
    }

    private fun mapLayerCore(input: JsonNode, id: String, level: Int, name: String, note: String): CoreLayer {
        return CoreLayer(
            type = LayerType.CORE,
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
        return OsLayer(type = LayerType.OS, id = layerId, level = level, name = name, note = note, nodeName = nodeName)
    }

    private fun mapLayerKeystore(input: JsonNode, id: String, level: Int, name: String, note: String): KeyStoreLayer {
        return KeyStoreLayer(
            type = LayerType.KEYSTORE,
            id = id,
            level = level,
            name = name,
            note = note,
            iceLayerId = input.get("iceLayerId").asText()
        )
    }

    // Initial version has OS layerId: {nodeId}-layer-0000 . This break in format makes it incompatible with input validation.
    private fun fixOsLayerId(idInput: String): String {
        if (!(idInput.endsWith("-layer-0000"))) return idInput

        val nodeId = idInput.substringBefore("-layer-0000")
        return nodeEntityService.createOsLayerId(nodeId)
    }

    private fun mapLayerStatusLight(input: JsonNode, id: String, level: Int, name: String, note: String): StatusLightLayer {
        val type = LayerType.valueOf(input.get("type").asText())
        return StatusLightLayer(
            type = type,
            id = id, level = level, name = name, note = note,
            appId = input.get("appId").asText(),
            status = input.get("status").asBoolean(),
            textForRed = input.get("textForRed").asText(),
            textForGreen = input.get("textForGreen").asText(),
        )
    }

    private fun mapLayerTripWire(input: JsonNode, id: String, level: Int, name: String, note: String): TripwireLayer {
        return TripwireLayer(
            type = LayerType.TRIPWIRE,
            id = id, level = level, name = name, note = note,
            countdown = input.get("countdown").asText(),
            shutdown = input.get("shutdown").asText(),
            coreLayerId = input.get("coreLayerId").asText(),
        )
    }

    private fun mapLayerScriptInteraction(input: JsonNode, id: String, level: Int, name: String, note: String): ScriptInteractionLayer {
        return ScriptInteractionLayer(
            type = LayerType.SCRIPT_INTERACTION,
            id = id, level = level, name = name, note = note,
            interactionKey =  input.get("interactionKey").asText(),
            message = input.get("message").asText(),
        )
    }

    private fun mapIceLayer(input: JsonNode, layerType: LayerType, id: String, level: Int, name: String, note: String): IceLayer {
        val strength = IceStrength.valueOf(input.get("strength").asText())

        return when (layerType) {
            LayerType.NETWALK_ICE -> NetwalkIceLayer(id, LayerType.NETWALK_ICE, level, name, note, strength, false)
            LayerType.TANGLE_ICE -> mapTangleIce(input, id, level, name, note, strength)
            LayerType.TAR_ICE -> TarIceLayer(id, LayerType.TAR_ICE, level, name, note, strength, false)
            LayerType.WORD_SEARCH_ICE -> WordSearchIceLayer(id, LayerType.WORD_SEARCH_ICE, level, name, note, strength, false)
            LayerType.PASSWORD_ICE -> mapPasswordIce(input, id, level, name, note, strength)
            LayerType.SWEEPER_ICE -> SweeperIceLayer(id, LayerType.SWEEPER_ICE, level, name, note, strength, false)
            else -> error("Unsupported ice layer type: $layerType")
        }
    }

    private fun mapPasswordIce(input: JsonNode, id: String, level: Int, name: String, note: String, strength: IceStrength): PasswordIceLayer {
        val password = input.get("password").asText()
        val hint = input.get("hint").asText()
        return PasswordIceLayer(id, LayerType.PASSWORD_ICE, level, name, note, strength, false, password, hint)
    }

    private fun mapTangleIce(input: JsonNode, id: String, level: Int, name: String, note: String, strength: IceStrength): TangleIceLayer {
        val clusters = if (input.has("clusters")) input.get("clusters").asInt() else 1
        return TangleIceLayer(id, LayerType.TANGLE_ICE, level, name, note, strength, false, clusters)
    }

    private fun importConnections(root: JsonNode): List<Connection> {
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
