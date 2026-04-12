package org.n1.av2.editor

import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.*
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.site.entity.enums.NodeType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/site")
class SiteRestController(
    private val editorService: EditorService,
    private val siteService: SiteService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
) {
    @GetMapping
    fun listSites(): List<SiteService.SiteListItem> {
        return siteService.getSitesList()
    }

    @GetMapping("/{siteId}")
    fun getSite(@PathVariable siteId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        return siteService.getSiteFull(siteId)
    }

    @GetMapping("/{siteId}/node")
    fun getNodes(@PathVariable siteId: String, userPrincipal: UserPrincipal): List<Node> {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        return nodeEntityService.findBySiteId(siteId)
    }

    @GetMapping("/{siteId}/node/{nodeId}")
    fun getNode(@PathVariable siteId: String, @PathVariable nodeId: String, userPrincipal: UserPrincipal): Node {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        return nodeEntityService.getById(nodeId)
    }

    @GetMapping("/{siteId}/connection")
    fun getConnections(@PathVariable siteId: String, userPrincipal: UserPrincipal): List<Connection> {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        return connectionEntityService.getAll(siteId)
    }

    class CreateSiteRequest(val name: String = "")

    @PostMapping
    fun createSite(@RequestBody request: CreateSiteRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteByName(request.name, userPrincipal)
        editorService.open(request.name)
        val siteProperties = sitePropertiesEntityService.findByName(request.name)!!
        return siteService.getSiteFull(siteProperties.siteId)
    }

    class EditSitePropertyRequest(
        val field: SitePropertyField,
        val value: String,
    )

    @PutMapping("/{siteId}/property")
    fun editSiteProperty(@PathVariable siteId: String, @RequestBody command: EditSitePropertyRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.updateSiteProperties(EditSiteProperty(siteId = siteId, field = command.field, value = command.value))
        return siteService.getSiteFull(siteId)
    }

    class AddNodeRequest(val x: Int = 0, val y: Int = 0, val type: NodeType = NodeType.TRANSIT_1, val networkId: String? = null)

    @PostMapping("/{siteId}/node")
    fun addNode(@PathVariable siteId: String, @RequestBody request: AddNodeRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.addNode(AddNode(siteId = siteId, x = request.x, y = request.y, type = request.type, networkId = request.networkId))
        return siteService.getSiteFull(siteId)
    }

    class MoveNodeRequest(val x: Int = 0, val y: Int = 0)

    @PutMapping("/{siteId}/node/{nodeId}/move")
    fun moveNode(@PathVariable siteId: String, @PathVariable nodeId: String, @RequestBody request: MoveNodeRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.moveNode(MoveNode(siteId = siteId, nodeId = nodeId, x = request.x, y = request.y))
        return siteService.getSiteFull(siteId)
    }

    class EditNetworkIdRequest(val value: String = "")

    @PutMapping("/{siteId}/node/{nodeId}/networkId")
    fun editNetworkId(@PathVariable siteId: String, @PathVariable nodeId: String, @RequestBody request: EditNetworkIdRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.editNetworkId(EditNetworkIdCommand(siteId = siteId, nodeId = nodeId, value = request.value))
        return siteService.getSiteFull(siteId)
    }

    @DeleteMapping("/{siteId}/node/{nodeId}")
    fun deleteNode(@PathVariable siteId: String, @PathVariable nodeId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.deleteNode(siteId, nodeId)
        return siteService.getSiteFull(siteId)
    }

    @DeleteMapping("/{siteId}/node/{nodeId}/connections")
    fun deleteConnections(@PathVariable siteId: String, @PathVariable nodeId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.deleteConnections(siteId, nodeId)
        return siteService.getSiteFull(siteId)
    }

    class AddConnectionRequest(val fromId: String = "", val toId: String = "")

    @PostMapping("/{siteId}/connection")
    fun addConnection(@PathVariable siteId: String, @RequestBody request: AddConnectionRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.addConnection(AddConnection(siteId = siteId, fromId = request.fromId, toId = request.toId))
        return siteService.getSiteFull(siteId)
    }

    class AddLayerRequest(val layerType: LayerType = LayerType.TEXT)

    @PostMapping("/{siteId}/node/{nodeId}/layer")
    fun addLayer(@PathVariable siteId: String, @PathVariable nodeId: String, @RequestBody request: AddLayerRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.addLayer(AddLayerCommand(siteId = siteId, nodeId = nodeId, layerType = request.layerType))
        return siteService.getSiteFull(siteId)
    }

    class EditLayerDataRequest(val key: String = "", val value: String = "")

    @PutMapping("/{siteId}/node/{nodeId}/layer/{layerId}")
    fun editLayerData(
        @PathVariable siteId: String,
        @PathVariable nodeId: String,
        @PathVariable layerId: String,
        @RequestBody request: EditLayerDataRequest,
        userPrincipal: UserPrincipal,
    ): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.editLayerData(EditLayerDataCommand(siteId = siteId, nodeId = nodeId, layerId = layerId, key = request.key, value = request.value))
        return siteService.getSiteFull(siteId)
    }

    @DeleteMapping("/{siteId}/node/{nodeId}/layer/{layerId}")
    fun removeLayer(@PathVariable siteId: String, @PathVariable nodeId: String, @PathVariable layerId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.removeLayer(RemoveLayerCommand(siteId = siteId, nodeId = nodeId, layerId = layerId))
        return siteService.getSiteFull(siteId)
    }

    class SwapLayerRequest(val fromId: String = "", val toId: String = "")

    @PutMapping("/{siteId}/node/{nodeId}/layer/swap")
    fun swapLayers(@PathVariable siteId: String, @PathVariable nodeId: String, @RequestBody request: SwapLayerRequest, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.swapLayers(SwapLayerCommand(siteId = siteId, nodeId = nodeId, fromId = request.fromId, toId = request.toId))
        return siteService.getSiteFull(siteId)
    }

    @PostMapping("/{siteId}/node/{nodeId}/layer/{layerId}/statusLightOption")
    fun addStatusLightOption(@PathVariable siteId: String, @PathVariable nodeId: String, @PathVariable layerId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.addStatusLightOption(EditorWsController.AddStatusLightOptionCommand(siteId, nodeId, layerId))
        return siteService.getSiteFull(siteId)
    }

    @DeleteMapping("/{siteId}/node/{nodeId}/layer/{layerId}/statusLightOption/{index}")
    fun deleteStatusLightOption(
        @PathVariable siteId: String,
        @PathVariable nodeId: String,
        @PathVariable layerId: String,
        @PathVariable index: Int,
        userPrincipal: UserPrincipal,
    ): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.deleteStatusLightOption(EditorWsController.DeleteStatusLightOptionCommand(siteId, nodeId, layerId, index))
        return siteService.getSiteFull(siteId)
    }

    @PostMapping("/{siteId}/snap")
    fun snap(@PathVariable siteId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.snap(siteId)
        return siteService.getSiteFull(siteId)
    }

    @PostMapping("/{siteId}/center")
    fun center(@PathVariable siteId: String, userPrincipal: UserPrincipal): SiteFull {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        editorService.center(siteId)
        return siteService.getSiteFull(siteId)
    }
}
