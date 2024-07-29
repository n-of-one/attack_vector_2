import {findElementById, updateArrayById} from "../../common/util/Immutable"
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
import {LayerType} from "../../common/enums/LayerTypes";
import {NodeScanStatus} from "../../common/enums/NodeStatus";

export const TRANSIT_1 = "transit_1"
export const TRANSIT_2 = "transit_2"
export const TRANSIT_3 = "transit_3"
export const TRANSIT_4 = "transit_4"
export const SYSCON = "syscon"
export const DATA_STORE = "data_store"
export const PASSCODE_STORE = "passcode_store"
export const RESOURCE_STORE = "resource_store"
export const ICE_1= "ice_1"
export const ICE_2 = "ice_2"
export const ICE_3 = "ice_3"
export const UNHACKABLE = "unhackable"
export const MANUAL_1 = "manual_1"
export const MANUAL_2 = "manual_2"
export const MANUAL_3 = "manual_3"

export type NodeTypeName = "transit_1" | "transit_2" | "transit_3" | "transit_4" | "syscon" | "data_store" | "passcode_store" | "resource_store" | "ice_1" | "ice_2" | "ice_3" | "unhackable" | "manual_1" | "manual_2" | "manual_3"


export interface LayerDetails {
    id: string
    type: LayerType
    level: number       // height of the layer. level 0 is always OS. Hack top level first.
    name: string
    note: string
    ice: boolean
    hacked: boolean

    nodeName?: string                 // OS layer
    text?: string                     // Text layer
    shutdown?: string                 // Layer Trip wire
    countdown?: string                // Layer Trip wire
    coreLayerId?: string              // Layer Trip wire

    strength? : "VERY_WEAK" | "WEAK" | "AVERAGE" | "STRONG" | "VERY_STRONG" | "ONYX"
    password? : string  // Password Ice layer
    hint?: string       // password Ice layer

    totalUnits?: number          // Tar Ice layer
    time1Level1Hacker?: string   // Tar Ice layer
    time1Level5Hacker?: string   // Tar Ice layer
    time5Level10Hackers?: string // Tar Ice layer

    appId?: string               // Status Light layer
    status?: boolean             // Status Light layer
    textForGreen?: string        // Status Light layer
    textForRed?: string          // Status Light layer

    iceLayerId?: string          // Keystore layer

    revealNetwork?: boolean       // Core layer

    clusters?: number             // Tangle Ice layer

}

export interface NodeI {
    id: string,
    siteId: string,
    type: NodeTypeName,
    x: number,
    y: number,
    ice: boolean,
    layers: Array<LayerDetails>,
    networkId: string

    hacked : boolean
    status: NodeScanStatus
    distance: number
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


const serverUpdateLayer = (update: {nodeId: string, layerId: string, layer: LayerDetails}, nodes: Array<NodeI>) => {
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

const serverAddLayer = (data: {nodeId: string, layer: LayerDetails}, nodes: Array<NodeI>) => {
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
