import dragAndDropReducer from "./reducer/DragAndDropReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import connectionsReducer from "./reducer/ConnectionsReducer";
import layoutReducer from "./reducer/LayoutReducer";
import {SiteDataReducer} from "./reducer/SiteDataReducer";
import {NodesReducer} from "./reducer/NodesReducer";
import currentNodeIdReducer from "./reducer/CurrentNodeIdReducer";
import currentLayerIdReducer from "./reducer/CurrentLayerIdReducer";
import siteStateReducer from "./reducer/SiteStateReducer";



const editorRootReducer = (state, action) => {
    const newState = {};
    newState.siteData = SiteDataReducer(state.siteData, action);
    newState.layout = layoutReducer(state.layout, action);
    newState.dragAndDrop = dragAndDropReducer(state.dragAndDrop, action);
    newState.theme = ThemeReducer(state.theme, action);
    newState.nodes = NodesReducer(state.nodes, action);
    newState.connections = connectionsReducer(state.connections, action);
    newState.currentNodeId = currentNodeIdReducer(state.currentNodeId, action, state.nodes);
    newState.currentLayerId = currentLayerIdReducer(state.currentLayerId, action, state.currentNodeId, state.nodes);
    newState.state = siteStateReducer(state.state, action);

    return newState;
};


export default editorRootReducer;