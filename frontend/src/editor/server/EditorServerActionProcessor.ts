import {webSocketConnection} from "../../common/server/WebSocketConnection"
import {NotificationType} from "../../common/util/Notification"
import {editorCanvas, LoadSiteData} from "../component/map/canvas/EditorCanvas"
import {MoveNodeI, NodeI} from "../reducer/NodesReducer"
import {Connection} from "../reducer/ConnectionsReducer"
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";

export const SERVER_SITE_FULL  = "SERVER_SITE_FULL"
export const SERVER_UPDATE_SITE_DATA = "SERVER_UPDATE_SITE_DATA"
export const SERVER_ADD_NODE = "SERVER_ADD_NODE"
export const SERVER_MOVE_NODE = "SERVER_MOVE_NODE"
export const SERVER_ADD_CONNECTION = "SERVER_ADD_CONNECTION"
export const SERVER_UPDATE_NETWORK_ID = "SERVER_UPDATE_NETWORK_ID"
export const SERVER_UPDATE_LAYER = "SERVER_UPDATE_LAYER"
export const SERVER_UPDATE_SITE_STATE = "SERVER_UPDATE_SITE_STATE"
export const SERVER_ADD_LAYER = "SERVER_ADD_LAYER"
export const SERVER_NODE_UPDATED = "SERVER_NODE_UPDATED"

export const initEditorServerActions = () => {

    initGenericServerActions()

    webSocketConnection.addAction(SERVER_SITE_FULL, (data: LoadSiteData) => {
        editorCanvas.loadSite(data)
    })

    webSocketConnection.addAction(SERVER_ADD_NODE, (data: NodeI) => {
        editorCanvas.addNode(data)
        editorCanvas.selectNode(data.id)
    })

    webSocketConnection.addAction(SERVER_MOVE_NODE, (data: MoveNodeI) => {
        editorCanvas.moveNode(data)
    })

    webSocketConnection.addAction(SERVER_ADD_CONNECTION, (connectionData: Connection) => {
        editorCanvas.addConnection(connectionData)
    })

}

export interface ServerNotification {
    type: NotificationType,
    title: string,
    message: string
}

