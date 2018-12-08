import {combineReducers} from 'redux'
import siteReducer from "./reducer/SiteReducer";
import DragAndDropReducer from "./reducer/DragAndDropReducer";
import ThemeReducer from "./reducer/ThemeReducer";
import NodesReducer from "./reducer/NodesReducer";
import ConnectionsReducer from "./reducer/ConnectionsReducer";
import CurrentNodeReducer from "./reducer/CurrentNodeReducer";

const editorReducer = combineReducers({
    site: siteReducer,
    dragAndDrop: DragAndDropReducer,
    theme: ThemeReducer,
    nodes: NodesReducer,
    connections: ConnectionsReducer,
    currentNode: CurrentNodeReducer,
});

export default editorReducer;