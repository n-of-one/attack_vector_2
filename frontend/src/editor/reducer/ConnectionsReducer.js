import {SERVER_SITE_FULL, SERVER_ADD_CONNECTION} from "../EditorActions";

export default (state = [], action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.connections;
        case SERVER_ADD_CONNECTION: return addConnection(action.data, state);
        default: return state;
    }
}

let addConnection = (data, connections) => {
    let connection = { ...data };
    return [ ...connections, connection ];
};
