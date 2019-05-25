import SiteDataReducer from "./reducer/SiteDataReducer";
import DragAndDropReducer from "./reducer/DragAndDropReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import NodesReducer from "./reducer/NodesReducer";
import ConnectionsReducer from "./reducer/ConnectionsReducer";
import LayoutReducer from "./reducer/LayoutReducer";
import CurrentServiceReducer from "./reducer/CurrentServiceReducer";
import {CurrentNodeReducer} from "./reducer/CurrentNodeReducer";



const editorReducer = (state, action) => {
    const newState = {};
    newState.siteData = SiteDataReducer(state.siteData, action);
    newState.layout = LayoutReducer(state.layout, action);
    newState.dragAndDrop = DragAndDropReducer(state.dragAndDrop, action);
    newState.theme = ThemeReducer(state.theme, action);
    newState.nodes = NodesReducer(state.nodes, action);
    newState.connections = ConnectionsReducer(state.connections, action);
    newState.currentNode = CurrentNodeReducer(state.currentNode, action, state.nodes);
    newState.currentService = CurrentServiceReducer(state.currentService, action, state.nodes);

    return newState;
};


export default editorReducer;