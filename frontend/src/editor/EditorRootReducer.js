import DragAndDropReducer from "./reducer/DragAndDropReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import ConnectionsReducer from "./reducer/ConnectionsReducer";
import LayoutReducer from "./reducer/LayoutReducer";
import {SiteDataReducer} from "./reducer/SiteDataReducer";
import {NodesReducer} from "./reducer/NodesReducer";
import CurrentNodeIdReducer from "./reducer/CurrentNodeIdReducer";
import CurrentLayerIdReducer from "./reducer/CurrentLayerIdReducer";
import SiteStateReducer from "./reducer/SiteStateReducer";



const editorRootReducer = (state, action) => {
    const newState = {};
    newState.siteData = SiteDataReducer(state.siteData, action);
    newState.layout = LayoutReducer(state.layout, action);
    newState.dragAndDrop = DragAndDropReducer(state.dragAndDrop, action);
    newState.theme = ThemeReducer(state.theme, action);
    newState.nodes = NodesReducer(state.nodes, action);
    newState.connections = ConnectionsReducer(state.connections, action);
    newState.currentNodeId = CurrentNodeIdReducer(state.currentNodeId, action, state.nodes);
    newState.currentLayerId = CurrentLayerIdReducer(state.currentLayerId, action, state.currentNodeId, state.nodes);
    newState.state = SiteStateReducer(state.state, action);

    return newState;
};


export default editorRootReducer;