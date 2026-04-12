import {webSocketConnection} from "./WebSocketConnection";
import {serverTime} from "./ServerTime";
import {notify} from "../util/Notification";
import {ServerNotification} from "../../editor/server/EditorServerActionProcessor";
import {SERVER_RECEIVE_CURRENT_USER, User} from "../users/CurrentUserReducer";
import {attachFontChangeListener, setFontSize} from "../util/FontSizeListener";

export const SERVER_NOTIFICATION = "SERVER_NOTIFICATION"
export const SERVER_ERROR = "SERVER_ERROR"
export const SERVER_DISCONNECT = "SERVER_DISCONNECT"
export const SERVER_TIME_SYNC = "SERVER_TIME_SYNC"
export const SERVER_USER_CONNECTION = "SERVER_USER_CONNECTION"
export const SERVER_OPEN_EDITOR = "SERVER_OPEN_EDITOR"
export const SERVER_ICE_HACKED = "SERVER_ICE_HACKED"
export const SERVER_RESET_ICE = "SERVER_RESET_ICE"

export const initGenericServerActions = ({fontSize = "dynamic"}: {fontSize: number | "dynamic"}) => {

    if (fontSize === "dynamic") {
        webSocketConnection.addAction(SERVER_RECEIVE_CURRENT_USER, (user: User) => {
            setFontSize(user.preferences.fontSize)
            attachFontChangeListener(user.id)
        })
    }
    else {
        setFontSize(fontSize)
    }

    webSocketConnection.addAction(SERVER_TIME_SYNC, (timeOnServer: string) => {
        serverTime.init(timeOnServer)
    })

    webSocketConnection.addAction(SERVER_NOTIFICATION, (data: ServerNotification) => {
        notify(data)
    })

    webSocketConnection.addAction(SERVER_DISCONNECT, () => {
        notify({type: 'fatal', message: 'Connection with server lost. Please refresh.'})
    })

    webSocketConnection.addAction(SERVER_ERROR, (data: { message: string, recoverable: boolean }) => {
        const type = data.recoverable ? 'error' : 'fatal'
        notify({type: type, message: data.message})
    })
}
