import {webSocketConnection} from "../../common/WebSocketConnection"
import {editorSiteId} from "../EditorRoot"

export const sendSiteDataChanged = ({field, value}: { field: string, value: string }) => {
    const payload = {siteId: editorSiteId, field, value}
    webSocketConnection.send("/av/editor/editSiteData", payload)
}

export const sendAddNode = ({x, y, type}: { x: number, y: number, type: string }) => {
    const payload = {siteId: editorSiteId, x, y, type}
    webSocketConnection.send("/av/editor/addNode", payload)
}

export const sendMoveNode = ({nodeId, x, y}: { nodeId: string, x: number, y: number }) => {
    const payload = {siteId: editorSiteId, nodeId, x, y}
    webSocketConnection.send("/av/editor/moveNode", payload)
}

export const sendAddConnection = ({fromId, toId}: { fromId: string, toId: string }) => {
    const payload = {siteId: editorSiteId, fromId, toId}
    webSocketConnection.send("/av/editor/addConnection", payload)
}

export const sendDeleteConnections = ({nodeId}: { nodeId: string }) => {
    const payload = {siteId: editorSiteId, nodeId}
    webSocketConnection.send("/av/editor/deleteConnections", payload)
}

export const sendDeleteNode = ({nodeId}: { nodeId: string }) => {
    const payload = {siteId: editorSiteId, nodeId}
    webSocketConnection.send("/av/editor/deleteNode", payload)
}

export const sendSnap = () => {
    const payload = {siteId: editorSiteId}
    webSocketConnection.send("/av/editor/snap", payload)
}

export const sendEditLayerData = ({nodeId, layerId, key, value}: { nodeId: string, layerId: string, key: string, value: string }) => {
    const payload = {siteId: editorSiteId, nodeId, layerId, key, value}
    webSocketConnection.send("/av/editor/editLayerData", payload)
}

export const sendEditNetworkId = ({nodeId, value}: { nodeId: string, value: string }) => {
    const payload = {siteId: editorSiteId, nodeId, value}
    webSocketConnection.send("/av/editor/editNetworkId", payload)
}

export const sendAddLayer = ({layerType, nodeId}: { layerType: string, nodeId: string }) => {
    const payload = {siteId: editorSiteId, layerType, nodeId}
    webSocketConnection.send("/av/editor/addLayer", payload)
}

export const sendRemoveLayer = ({nodeId, layerId}: { nodeId: string, layerId: string }) => {
    const payload = {siteId: editorSiteId, nodeId, layerId}
    webSocketConnection.send("/av/editor/removeLayer", payload)
}

export const sendSwapLayers = ({nodeId, fromId, toId}: {nodeId: string, fromId: string, toId: string}) => {
    const payload = {siteId: editorSiteId, nodeId, fromId, toId}
    webSocketConnection.send("/av/editor/swapLayers", payload)
}

