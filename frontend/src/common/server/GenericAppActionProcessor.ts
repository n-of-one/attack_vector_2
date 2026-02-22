import {webSocketConnection} from "./WebSocketConnection";
import {SERVER_HACKER_DC, SERVER_HACKER_LEAVE_SITE} from "../../hacker/RunServerActionProcessor";

const SERVER_LEAVE_NODE = "SERVER_LEAVE_NODE"

export const initGenericAppActions = () => {

    webSocketConnection.addAction(SERVER_LEAVE_NODE, () => {
        window.close()
    })
}
