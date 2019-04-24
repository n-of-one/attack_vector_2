import {combineReducers} from 'redux'
import SiteDataReducer from "./reducer/SiteDataReducer";
import DragAndDropReducer from "./reducer/DragAndDropReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import NodesReducer from "./reducer/NodesReducer";
import ConnectionsReducer from "./reducer/ConnectionsReducer";
import CurrentNodeIdReducer from "./reducer/CurrentNodeIdReducer";
import LayoutReducer from "./reducer/LayoutReducer";

const editorReducer = combineReducers({
    siteData: SiteDataReducer,
    layout: LayoutReducer,
    dragAndDrop: DragAndDropReducer,
    theme: ThemeReducer,
    nodes: NodesReducer,
    connections: ConnectionsReducer,
    currentNodeId: CurrentNodeIdReducer,
});

export default editorReducer;