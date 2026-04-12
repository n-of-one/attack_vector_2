import {AnyAction} from "redux";
import {SELECT_NODE} from "./CurrentNodeIdReducer";
import {SERVER_ADD_LAYER, SERVER_NODE_UPDATED, SERVER_UPDATE_LAYER} from "../server/EditorServerActionProcessor";
import {SELECT_LAYER} from "./CurrentLayerIdReducer";
import {LayerDetails, NodeI} from "../../common/sites/SiteModel";
import {LayerType} from "../../common/enums/LayerTypes";

export const TEXT_INPUT_CHANGED = "TEXT_INPUT_CHANGED";

export const editorTerminalPreviewReducer = (state: string = "", currentNodeId: string | null, nodes: Array<NodeI>, currentLayerId: string | null, action: AnyAction): string => {
    switch (action.type) {
        case TEXT_INPUT_CHANGED:
            return action.text
        case SELECT_NODE :
            return updateForSelectNode(action.data, nodes)
        case SERVER_ADD_LAYER :
            return updateForNewLayer(action.data, currentNodeId, state)
        case SELECT_LAYER:
            return updateFromLayer(action.layerId, currentNodeId, nodes, state)
        case SERVER_NODE_UPDATED:
            return updateFromLayer(action.data.layerId, currentNodeId, nodes, state)
        case SERVER_UPDATE_LAYER:
            return serverUpdateLayer(action.data, currentLayerId, state)

        default:
            return state
    }
}

const updateForSelectNode = (newNodeId: string | null, nodes: Array<NodeI>): string => {
    if (!newNodeId) return ""
    const node = nodes.find(node => node.id === newNodeId)
    if (!node) return ""
    const layer = node.layers[node.layers.length - 1];
    return forLayer(layer)
}

const updateFromLayer = (selectedLayer: string | null, currentNodeId: string | null, nodes: Array<NodeI>, state: string): string => {
    if (!currentNodeId || !selectedLayer) return state
    const node = nodes.find(node => node.id === currentNodeId)
    if (!node) return state
    const layer = node.layers.find(layer => layer.id === selectedLayer)
    return forLayer(layer)
}

const serverUpdateLayer = (update: { nodeId: string, layerId: string, layer: LayerDetails }, currentLayerId: string | null, state: string): string => {
    if (update.layerId !== currentLayerId) {
        return state
    }
    return forLayer(update.layer);
}


const updateForNewLayer = (data: { nodeId: string, layer: LayerDetails }, currentNodeId: string | null, state: string): string => {
    if (currentNodeId !== data.nodeId) {
        return state
    }
    return forLayer(data.layer)
}

const forLayer = (layer: LayerDetails | null | undefined): string => {
    if (!layer) return ""
    switch (layer.type) {
        case LayerType.TEXT:
            return layer.text as string
        case LayerType.SCRIPT_INTERACTION:
            return layer.message as string
        default:
            return ""
    }

}
