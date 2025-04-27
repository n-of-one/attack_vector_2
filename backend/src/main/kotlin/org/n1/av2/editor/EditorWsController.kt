package org.n1.av2.editor

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class EditorWsController(
    private val editorService: EditorService,
    private val userTaskRunner: UserTaskRunner
) {
    @MessageMapping("/editor/open")
    fun open(siteName: String, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteByName(siteName, userPrincipal)
        userTaskRunner.runTask("/editor/open", userPrincipal) { editorService.open(siteName) }
    }

    @MessageMapping("/editor/enter")
    fun siteFull(siteId: String, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(siteId, userPrincipal)
        userTaskRunner.runTask("/editor/enter", userPrincipal) { editorService.enter(siteId) }
    }

    @MessageMapping("/editor/addNode")
    fun addNode(command: AddNode, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/addNode", userPrincipal) { editorService.addNode(command) }
    }

    @MessageMapping("/editor/moveNode")
    fun moveNode(command: MoveNode, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/moveNode", userPrincipal) { editorService.moveNode(command) }
    }

    @MessageMapping("/editor/addConnection")
    fun addConnection(command: AddConnection, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/addConnection", userPrincipal) { editorService.addConnection(command) }
    }

    @MessageMapping("/editor/editSiteProperty")
    fun editSiteData(command: EditSiteProperty, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/editSiteProperty", userPrincipal) { editorService.updateSiteProperties(command) }
    }

    data class DeleteCommand(val siteId: String = "", val nodeId: String = "" )
    @MessageMapping("/editor/deleteConnections")
    fun deleteConnections(command: DeleteCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/deleteConnections", userPrincipal) { editorService.deleteConnections(command.siteId, command.nodeId) }
    }

    @MessageMapping("/editor/deleteNode")
    fun deleteNode(command: DeleteCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/deleteNode", userPrincipal) { editorService.deleteNode(command.siteId, command.nodeId) }
    }

    data class SiteCommand(val siteId: String = "")
    @MessageMapping("/editor/snap")
    fun snap(command: SiteCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/snap", userPrincipal) { editorService.snap(command.siteId) }
    }

    @MessageMapping("/editor/center")
    fun center(command: SiteCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/center", userPrincipal) { editorService.center(command.siteId) }
    }

    @MessageMapping("/editor/editNetworkId")
    fun editNetworkId(command: EditNetworkIdCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/editNetworkId", userPrincipal) { editorService.editNetworkId(command) }
    }

    @MessageMapping("/editor/editLayerData")
    fun editServiceData(command: EditLayerDataCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/editLayerData", userPrincipal) { editorService.editLayerData(command) }
    }

    @MessageMapping("/editor/addLayer")
    fun addLayer(command: AddLayerCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/addLayer", userPrincipal) { editorService.addLayer(command) }
    }

    @MessageMapping("/editor/removeLayer")
    fun removeService(command: RemoveLayerCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/removeLayer", userPrincipal) { editorService.removeLayer(command) }
    }

    @MessageMapping("/editor/swapLayers")
    fun swapServiceLayer(command: SwapLayerCommand, userPrincipal: UserPrincipal) {
        editorService.validateAccessToSiteById(command.siteId, userPrincipal)
        userTaskRunner.runTask("/editor/swapLayers", userPrincipal) { editorService.swapLayers(command) }
    }

}
