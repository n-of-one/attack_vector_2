import {defaultDragAndDropState, dragAndDropReducer, DragAndDropState} from "./reducer/DragAndDropReducer"
import {themeReducer} from "../common/reducer/ThemeReducer"
import {Connection, connectionsReducer} from "./reducer/ConnectionsReducer"
import {SiteProperties, sitePropertiesDefault, SitePropertiesReducer} from "./reducer/SitePropertiesReducer"
import {currentNodeIdReducer} from "./reducer/CurrentNodeIdReducer"
import {currentLayerIdReducer} from "./reducer/CurrentLayerIdReducer"
import {siteStateDefault, SiteStateI, siteStateReducer} from "./reducer/SiteStateReducer"
import {AnyAction} from "redux"
import {editorNodesReducer} from "./reducer/EditorNodesReducer";
import {NodeI} from "../common/sites/SiteModel";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {allCoresReducer, CoreInfo} from "./reducer/AllCoresReducer";
import {currentUserReducer, User} from "../common/users/CurrentUserReducer";

export interface EditorState {
    siteProperties: SiteProperties,
    dragAndDrop: DragAndDropState,
    theme: string,
    nodes: Array<NodeI>,
    connections: Array<Connection>,
    currentNodeId: string | null,
    currentLayerId: string | null,
    state : SiteStateI,
    sites: Array<SiteInfo>,
    allCores: CoreInfo[],
    currentUser: User,
}

export const editorRootDefaultState = {
    siteProperties: { ...sitePropertiesDefault},
    dragAndDrop: defaultDragAndDropState,
    theme: "frontier",
    nodes: [],
    connections: [],
    currentNodeId: null,
    currentLayerId: null,
    state: siteStateDefault,
    sites: [],
    allCores: [],
}

export const editorRootReducer = (state:EditorState, action: AnyAction): EditorState => {
    const nodes = (state.nodes) ? state.nodes : []
    return {
        siteProperties: SitePropertiesReducer(state.siteProperties, action),
        dragAndDrop: dragAndDropReducer(state.dragAndDrop, action),
        theme: themeReducer(state.theme, action),
        nodes: editorNodesReducer(state.nodes, action),
        connections: connectionsReducer(state.connections, action),
        currentNodeId: currentNodeIdReducer(state.currentNodeId, action),
        currentLayerId: currentLayerIdReducer(state.currentLayerId, action, state.currentNodeId, nodes),
        state: siteStateReducer(state.state, action),
        sites: sitesReducer(state.sites, action),
        allCores: allCoresReducer(state.allCores, action),
        currentUser: currentUserReducer(state.currentUser, action),
    }
}
