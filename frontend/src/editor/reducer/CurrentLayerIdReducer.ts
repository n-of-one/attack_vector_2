import {findElementById} from "../../common/Immutable"
import {AnyAction} from "redux"
import {NodeI} from "./NodesReducer"
import {SERVER_ADD_LAYER, SERVER_NODE_UPDATED} from "../server/EditorServerActionProcessor"
import {SELECT_NODE} from "./CurrentNodeIdReducer"

export const SELECT_LAYER = "SELECT_LAYER"

export const currentLayerIdReducer = (state: string | null = null, action: AnyAction, currentNodeId: string | null | undefined, nodes: Array<NodeI>) => {
    switch(action.type) {
        case SELECT_NODE : return selectLayerFromSelectNode(action.data, nodes)
        case SERVER_ADD_LAYER : return action.data.layer.id
        case SELECT_LAYER: return action.layerId
        case SERVER_NODE_UPDATED: return serverNodeUpdated(action.data.layerId, state)
        default: return state
    }
}

const selectLayerFromSelectNode = (nodeId: string, nodes: Array<NodeI>) => {
    if (nodeId === null) {
        return null
    }
    const node = findElementById(nodes, nodeId)

    const layerToFind = node.layers.length - 1
    const layerToSelect = node.layers[layerToFind]
    return layerToSelect.id
}


const serverNodeUpdated = (layerId: string | null, state: string | null) => {
    return layerId ? layerId : state
}
