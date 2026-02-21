import {AnyAction} from "redux"
import {SERVER_SITE_FULL, SERVER_UPDATE_LAYER} from "../editor/server/EditorServerActionProcessor";
import {LayerDetails, NodeI} from "../common/sites/SiteModel";
import {TEXT_INPUT_CHANGED} from "../editor/reducer/editorTerminalPreviewReducer";
import {LayerType} from "../common/enums/LayerTypes";
import {ScriptType, SERVER_SCRIPT_TYPES} from "../common/script/type/ScriptTypeReducer";

export interface EditorTextState {
    terminalPreview: string,
    type: string,
    header: string,

    // type: Layer
    siteId: string,
    nodeId: string,
    layerId: string,
    key: string,
    initialValue: string,

    // type: SCRIPT_EFFECT
    scriptTypeId: string,
    effectNumber: number,
}

export const editorTextRootReducer = (state: EditorTextState, action: AnyAction): EditorTextState => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return deriveText(action.data.nodes, state)
        case SERVER_UPDATE_LAYER :
            return serverUpdateLayer(action.data, state)
        case TEXT_INPUT_CHANGED:
            return {...state, terminalPreview: action.text}
        case SERVER_SCRIPT_TYPES:
            return pareScriptTypes(action.data, state)

        default:
            return state
    }
}

const deriveText = (nodes: Array<NodeI>, state: EditorTextState): EditorTextState => {

    const myNode = nodes.find(node => node.id === state.nodeId)
    if (myNode === undefined) return state

    const myLayer = myNode.layers.find(layer => layer.id === state.layerId)
    if (myLayer === undefined) return state

    const text = textFromLayer(myLayer);
    return {...state, terminalPreview: text, initialValue: text}
}

const serverUpdateLayer = (update: { nodeId: string, layerId: string, layer: LayerDetails }, state: EditorTextState): EditorTextState => {
    if (update.layerId !== state.layerId) {
        return state
    }

    const text = textFromLayer(update.layer);
    return {...state, terminalPreview: text, initialValue: text}
}

const textFromLayer = (layer: LayerDetails): string => {
    switch (layer.type) {
        case LayerType.TEXT:
            return layer.text!!
        case LayerType.SCRIPT_INTERACTION:
            return layer.message!!
        default:
            return ""
    }
}


const pareScriptTypes = (data: ScriptType[], state: EditorTextState): EditorTextState => {
    const ourType = data.find(type => type.id === state.scriptTypeId)
    if (ourType === undefined) return forScriptEffectValue("ERROR: script type not found", state)

    const effectIndex = state.effectNumber - 1; // effect numbers are 1 based because they are shown to players.

    if (effectIndex >= ourType.effects.length) return forScriptEffectValue("ERROR: script effect not found", state)

    const value = ourType.effects[effectIndex].value || ""
    return forScriptEffectValue(value, state)
}

const forScriptEffectValue = (value: string, state: EditorTextState): EditorTextState => {
    return {...state, initialValue: value, terminalPreview: value}
}
