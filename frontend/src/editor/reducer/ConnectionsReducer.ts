import {SERVER_SITE_FULL, SERVER_ADD_CONNECTION} from "../EditorActions";
import {AnyAction} from "redux";

export interface Connection {
    id: string,
    siteId: string,
    fromId: string,
    toId: string,
}
const connectionsReducer = (state: Array<Connection> = [], action: AnyAction) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.connections;
        case SERVER_ADD_CONNECTION: return addConnection(action.data, state);
        default: return state;
    }
}


let addConnection = (connection: Connection, connections: Array<Connection>) => {
    return [ ...connections, connection ];
};

export default connectionsReducer;
