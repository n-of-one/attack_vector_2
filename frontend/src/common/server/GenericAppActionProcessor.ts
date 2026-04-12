import {webSocketConnection} from "./WebSocketConnection";

const SERVER_LEAVE_NODE = "SERVER_LEAVE_NODE"

export const initGenericAppActions = () => {

    webSocketConnection.addAction(SERVER_LEAVE_NODE, () => {
        window.close()
    })
}
