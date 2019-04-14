import {combineReducers} from 'redux'
import SiteReducer from "./reducer/SiteReducer";
import DragAndDropReducer from "./reducer/DragAndDropReducer";
import ThemeReducer from "./reducer/ThemeReducer";
import NodesReducer from "./reducer/NodesReducer";
import ConnectionsReducer from "./reducer/ConnectionsReducer";
import CurrentNodeIdReducer from "./reducer/CurrentNodeIdReducer";

const editorReducer = combineReducers({
    site: SiteReducer,
    dragAndDrop: DragAndDropReducer,
    theme: ThemeReducer,
    nodes: NodesReducer,
    connections: ConnectionsReducer,
    currentNodeId: CurrentNodeIdReducer,
});

export default editorReducer;