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
import {LayerDetails, NodeI} from "../../common/sites/SiteModel";


export interface MoveNodeI {nodeId: string, x: number, y: number}

export const editorNodesReducer = (state: Array<NodeI> = [], action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return siteFullForEditor(action.data.nodes)
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

const siteFullForEditor = (nodes: Array<NodeI>): Array<NodeI> => {
    return nodes.map(node => mapNode(node));
}

const mapNode = (node: NodeI): NodeI =>  {
    let changed = false
    const mappedLayers = node.layers.map(layer => {
       if (!layer.original) {
           return layer
       } else {
           changed = true
           return layer.original
       }
    });
    if (!changed) return node
    return {...node, layers: mappedLayers}
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

    return updateArrayById(newNodeLayers, nodes, update.nodeId)
}


const serverUpdateNetworkId = (update: {nodeId: string,  networkId: string}, nodes: Array<NodeI>) => {
    const newNodeData = {networkId: update.networkId}
    return updateArrayById(newNodeData, nodes, update.nodeId)
}

const serverAddLayer = (data: {nodeId: string, layer: LayerDetails}, nodes: Array<NodeI>) => {
    const node = findElementById(nodes, data.nodeId)
    const layer = data.layer
    const newLayers = [...node.layers, layer]

    const newNodeLayers = {layers: newLayers}

    return updateArrayById(newNodeLayers, nodes, data.nodeId)

}

const serverRemoveLayer = (node: NodeI, nodes: Array<NodeI>) => {
    return updateArrayById(node, nodes, node.id)
}
