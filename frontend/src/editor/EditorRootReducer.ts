import dragAndDropReducer, {defaultDragAndDropState, DragAndDropState} from "./reducer/DragAndDropReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import connectionsReducer, {Connection} from "./reducer/ConnectionsReducer";
import layoutReducer, {defaultLayout, Layout} from "./reducer/LayoutReducer";
import {SiteData, siteDataDefaultState, SiteDataReducer} from "./reducer/SiteDataReducer";
import {Node, NodesReducer} from "./reducer/NodesReducer";
import currentNodeIdReducer from "./reducer/CurrentNodeIdReducer";
import currentLayerIdReducer from "./reducer/CurrentLayerIdReducer";
import siteStateReducer, {defaultState, SiteStateI} from "./reducer/SiteStateReducer";
import {AnyAction} from "redux";

export interface EditorState {
    siteData: SiteData,
    layout: Layout,
    dragAndDrop: DragAndDropState,
    theme: string,
    nodes: Array<Node>,
    connections: Array<Connection>,
    currentNodeId: string | null,
    currentLayerId: string | null,
    state : SiteStateI


}

export const editorRootDefaultState = {
    siteData: { ...siteDataDefaultState, id: "to be overwritten"},
    layout: defaultLayout,
    dragAndDrop: defaultDragAndDropState,
    theme: "frontier",
    nodes: [],
    connections: [],
    currentNodeId: null,
    currentLayerId: null,
    state : defaultState
};


const editorRootReducer = (state:EditorState, action: AnyAction): EditorState => {
    const nodes = (state.nodes) ? state.nodes : []
    return {
        siteData: SiteDataReducer(state.siteData, action),
        layout: layoutReducer(state.layout, action),
        dragAndDrop: dragAndDropReducer(state.dragAndDrop, action),
        theme: ThemeReducer(state.theme, action),
        nodes: NodesReducer(state.nodes, action),
        connections: connectionsReducer(state.connections, action),
        currentNodeId: currentNodeIdReducer(state.currentNodeId, action),
        currentLayerId: currentLayerIdReducer(state.currentLayerId, action, state.currentNodeId, nodes),
        state: siteStateReducer(state.state, action)
    }
};


export default editorRootReducer;