import {dragAndDropReducer, defaultDragAndDropState, DragAndDropState} from "./reducer/DragAndDropReducer"
import {themeReducer} from "../common/reducer/ThemeReducer"
import {connectionsReducer, Connection} from "./reducer/ConnectionsReducer"
import {layoutReducer, defaultLayout, Layout} from "./reducer/LayoutReducer"
import {SiteData, siteDataDefaultState, SiteDataReducer} from "./reducer/SiteDataReducer"
import {NodeI, nodesReducer} from "./reducer/NodesReducer"
import {currentNodeIdReducer} from "./reducer/CurrentNodeIdReducer"
import {currentLayerIdReducer} from "./reducer/CurrentLayerIdReducer"
import {siteStateReducer, defaultState, SiteStateI} from "./reducer/SiteStateReducer"
import {AnyAction} from "redux"

export interface EditorState {
    siteData: SiteData,
    layout: Layout,
    dragAndDrop: DragAndDropState,
    theme: string,
    nodes: Array<NodeI>,
    connections: Array<Connection>,
    currentNodeId: string | null,
    currentLayerId: string | null,
    state : SiteStateI
}

export const editorRootDefaultState: EditorState = {
    siteData: { ...siteDataDefaultState},
    layout: defaultLayout,
    dragAndDrop: defaultDragAndDropState,
    theme: "frontier",
    nodes: [],
    connections: [],
    currentNodeId: null,
    currentLayerId: null,
    state : defaultState
}

export const editorRootReducer = (state:EditorState, action: AnyAction): EditorState => {
    const nodes = (state.nodes) ? state.nodes : []
    return {
        siteData: SiteDataReducer(state.siteData, action),
        layout: layoutReducer(state.layout, action),
        dragAndDrop: dragAndDropReducer(state.dragAndDrop, action),
        theme: themeReducer(state.theme, action),
        nodes: nodesReducer(state.nodes, action),
        connections: connectionsReducer(state.connections, action),
        currentNodeId: currentNodeIdReducer(state.currentNodeId, action),
        currentLayerId: currentLayerIdReducer(state.currentLayerId, action, state.currentNodeId, nodes),
        state: siteStateReducer(state.state, action)
    }
}
