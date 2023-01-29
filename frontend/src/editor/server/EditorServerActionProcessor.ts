import {webSocketConnection} from "../../common/WebSocketConnection"
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION, SERVER_TIME_SYNC} from "../../common/enums/CommonActions"
import {NotificationType, notify} from "../../common/Notification"
import {Dispatch} from "redux"
import {TERMINAL_RECEIVE} from "../../common/terminal/TerminalActions"
import {serverTime} from "../../common/ServerTime"
import {editorCanvas, LoadSiteData} from "../component/middle/middle/EditorCanvas"
import {MoveNodeI, NodeI} from "../reducer/NodesReducer"
import {Connection} from "../reducer/ConnectionsReducer"

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

export const initEditorServerActions = (dispatch: Dispatch) => {

    webSocketConnection.addAction(SERVER_TIME_SYNC, (timeOnServer: string) => {
        serverTime.init(timeOnServer)
    })

    webSocketConnection.addAction(SERVER_NOTIFICATION, (data: ServerNotification) => {
        notify(data)
    })

    webSocketConnection.addAction(SERVER_FORCE_DISCONNECT, (data: ServerNotification) => {
        notify(data)
    })

    webSocketConnection.addAction(SERVER_DISCONNECT, () => {
        notify({type: 'fatal', message: 'Connection with server lost. Please refresh browser.'})
        dispatch({type: TERMINAL_RECEIVE, data: "[b warn]Connection with server lost. Please refresh browser."})
    })

    webSocketConnection.addAction(SERVER_ERROR, (data: { message: string }) => {
        notify({type: 'error', message: data.message})
    })

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

