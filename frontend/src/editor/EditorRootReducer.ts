import {defaultDragAndDropState, dragAndDropReducer, DragAndDropState} from "./reducer/DragAndDropReducer"
import {themeReducer} from "../common/reducer/ThemeReducer"
import {Connection, connectionsReducer} from "./reducer/ConnectionsReducer"
import {SiteProperties, sitePropertiesDefault, SitePropertiesReducer} from "./reducer/SitePropertiesReducer"
import {NodeI, nodesReducer} from "./reducer/NodesReducer"
import {currentNodeIdReducer} from "./reducer/CurrentNodeIdReducer"
import {currentLayerIdReducer} from "./reducer/CurrentLayerIdReducer"
import {siteStateDefault, SiteStateI, siteStateReducer} from "./reducer/SiteStateReducer"
import {AnyAction} from "redux"

export interface EditorState {
    siteProperties: SiteProperties,
    dragAndDrop: DragAndDropState,
    theme: string,
    nodes: Array<NodeI>,
    connections: Array<Connection>,
    currentNodeId: string | null,
    currentLayerId: string | null,
    state : SiteStateI,
}

export const editorRootDefaultState: EditorState = {
    siteProperties: { ...sitePropertiesDefault},
    dragAndDrop: defaultDragAndDropState,
    theme: "frontier",
    nodes: [],
    connections: [],
    currentNodeId: null,
    currentLayerId: null,
    state : siteStateDefault
}

export const editorRootReducer = (state:EditorState, action: AnyAction): EditorState => {
    const nodes = (state.nodes) ? state.nodes : []
    return {
        siteProperties: SitePropertiesReducer(state.siteProperties, action),
        dragAndDrop: dragAndDropReducer(state.dragAndDrop, action),
        theme: themeReducer(state.theme, action),
        nodes: nodesReducer(state.nodes, action),
        connections: connectionsReducer(state.connections, action),
        currentNodeId: currentNodeIdReducer(state.currentNodeId, action),
        currentLayerId: currentLayerIdReducer(state.currentLayerId, action, state.currentNodeId, nodes),
        state: siteStateReducer(state.state, action)
    }
}
