import {webSocketConnection} from "../../common/server/WebSocketConnection"
import {editorSiteId} from "../EditorRoot"

export const sendSitePropertyChanged = ({field, value}: { field: string, value: string|boolean }) => {
    const payload = {siteId: editorSiteId, field, value}
    webSocketConnection.send("/editor/editSiteProperty", payload)
}

export const sendAddNode = ({x, y, type}: { x: number, y: number, type: string }) => {
    const payload = {siteId: editorSiteId, x, y, type}
    webSocketConnection.send("/editor/addNode", payload)
}

export const sendMoveNode = ({nodeId, x, y}: { nodeId: string, x: number, y: number }) => {
    const payload = {siteId: editorSiteId, nodeId, x, y}
    webSocketConnection.send("/editor/moveNode", payload)
}

export const sendAddConnection = ({fromId, toId}: { fromId: string, toId: string }) => {
    const payload = {siteId: editorSiteId, fromId, toId}
    webSocketConnection.send("/editor/addConnection", payload)
}

export const sendDeleteConnections = ({nodeId}: { nodeId: string }) => {
    const payload = {siteId: editorSiteId, nodeId}
    webSocketConnection.send("/editor/deleteConnections", payload)
}

export const sendDeleteNode = ({nodeId}: { nodeId: string }) => {
    const payload = {siteId: editorSiteId, nodeId}
    webSocketConnection.send("/editor/deleteNode", payload)
}

export const sendSnap = () => {
    const payload = {siteId: editorSiteId}
    webSocketConnection.send("/editor/snap", payload)
}

export const sendCenter = () => {
    const payload = {siteId: editorSiteId}
    webSocketConnection.send("/editor/center", payload)
}

export const sendEditLayerData = ({nodeId, layerId, key, value}: { nodeId: string, layerId: string, key: string, value: string }) => {
    const payload = {siteId: editorSiteId, nodeId, layerId, key, value}
    webSocketConnection.send("/editor/editLayerData", payload)
}

export const sendEditNetworkId = ({nodeId, value}: { nodeId: string, value: string }) => {
    const payload = {siteId: editorSiteId, nodeId, value}
    webSocketConnection.send("/editor/editNetworkId", payload)
}

export const sendRemoveLayer = ({nodeId, layerId}: { nodeId: string, layerId: string }) => {
    const payload = {siteId: editorSiteId, nodeId, layerId}
    webSocketConnection.send("/editor/removeLayer", payload)
}

export const sendSwapLayers = ({nodeId, fromId, toId}: {nodeId: string, fromId: string, toId: string}) => {
    const payload = {siteId: editorSiteId, nodeId, fromId, toId}
    webSocketConnection.send("/editor/swapLayers", payload)
}

