import {webSocketConnection} from "../common/server/WebSocketConnection";

export const SERVER_OPEN_EDITOR = "SERVER_OPEN_EDITOR"

export const initGmServerActions = () => {

    webSocketConnection.addAction(SERVER_OPEN_EDITOR, (data: { id: string }) => {
        window.open("/edit/" + data.id, data.id)
    })
}
