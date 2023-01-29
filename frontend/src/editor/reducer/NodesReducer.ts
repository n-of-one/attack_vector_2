import {findElementById, updateArrayById} from "../../common/Immutable"
import {AnyAction} from "redux"
import {
    SERVER_ADD_LAYER,
    SERVER_ADD_NODE,
    SERVER_MOVE_NODE,
    SERVER_NODE_UPDATED,
    SERVER_SITE_FULL,
    SERVER_UPDATE_LAYER,
    SERVER_UPDATE_NETWORK_ID
} from "../server/EditorServerActionProcessor"

export enum NodeType {
    TRANSIT_1 = "TRANSIT_1",
    TRANSIT_2 = "TRANSIT_2",
    TRANSIT_3 = "TRANSIT_3",
    TRANSIT_4 = "TRANSIT_4",
    SYSCON = "SYSCON",
    DATA_STORE = "DATA_STORE",
    PASSCODE_STORE = "PASSCODE_STORE",
    RESOURCE_STORE = "RESOURCE_STORE",
    ICE_1 = "ICE_1",
    ICE_2 = "ICE_2",
    ICE_3 = "ICE_3",
    UNHACKABLE = "UNHACKABLE",
    MANUAL_1 = "MANUAL_1",
    MANUAL_2 = "MANUAL_2",
    MANUAL_3 = "MANUAL_3",
}

export enum LayerType {
    OS= "OS",
    TEXT= "TEXT",
    TIMER_TRIGGER= "TIMER_TRIGGER",
    ICE_PASSWORD= "ICE_PASSWORD",
    ICE_TANGLE= "ICE_TANGLE",
}

export interface EditorLayerDetails {
    id: string
    type: LayerType
    level: number       // height of the layer. level 0 is always OS. Hack top level first.
    name: string
    note: string

    nodeName?: string   // OS layer
    text?: string       // Text layer
    minutes?: number    // Layer Timer Trigger
    seconds?: number    // Layer Timer Trigger

    strength? : "VERY_WEAK" | "WEAK" | "AVERAGE" | "STRONG" | "VERY_STRONG" | "IMPENETRABLE" | "UNKNOWN"
    password? : string  // Password Ice layer
    hint?: string       // password Ice layer


}

export interface NodeI {
    id: string,
    siteId: string,
    type: NodeType,
    x: number,
    y: number,
    ice: boolean,
    layers: Array<EditorLayerDetails>,
    networkId: string
}

export interface MoveNodeI {nodeId: string, x: number, y: number}

export const nodesReducer = (state: Array<NodeI> = [], action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.nodes
        case SERVER_ADD_NODE:
            return addNode(action.data, state)
        case SERVER_MOVE_NODE:
            return moveNode(action.data, state)
        case SERVER_UPDATE_NETWORK_ID:
            return serverUpdateNetworkId(action.data, state)
        case SERVER_UPDATE_LAYER :
            return serverUpdateLayer(action.data, state)
        case SERVER_ADD_LAYER :
            return serverAddLayer(action.data, state)
        case SERVER_NODE_UPDATED :
            return serverRemoveLayer(action.data.node, state)

        default:
            return state
    }
}

const addNode = (data: NodeI, nodeList: Array<NodeI>): Array<NodeI> => {
    const node = {...data, connections: []}
    return [...nodeList, node]
}



const moveNode = (data: MoveNodeI, nodeList: Array<NodeI>) => {
    const newNodeData = {x: data.x, y: data.y}
    return updateArrayById(newNodeData, nodeList, data.nodeId)
}


const serverUpdateLayer = (update: {nodeId: string, layerId: string, layer: EditorLayerDetails}, nodes: Array<NodeI>) => {
    const node = findElementById(nodes, update.nodeId)
    const newLayers = updateArrayById(update.layer, node.layers, update.layerId)

    const newNodeLayers = {layers: newLayers}

    const newNodes = updateArrayById(newNodeLayers, nodes, update.nodeId)
    return newNodes
}


const serverUpdateNetworkId = (update: {nodeId: string,  networkId: string}, nodes: Array<NodeI>) => {
    const newNodeData = {networkId: update.networkId}
    const newNodes = updateArrayById(newNodeData, nodes, update.nodeId)
    return newNodes
}

const serverAddLayer = (data: {nodeId: string, layer: EditorLayerDetails}, nodes: Array<NodeI>) => {
    const node = findElementById(nodes, data.nodeId)
    const layer = data.layer
    const newLayers = [...node.layers, layer]

    const newNodeLayers = {layers: newLayers}

    const newNodes = updateArrayById(newNodeLayers, nodes, data.nodeId)
    return newNodes

}

const serverRemoveLayer = (node: NodeI, nodes: Array<NodeI>) => {
    const newNodes = updateArrayById(node, nodes, node.id)
    return newNodes
}
