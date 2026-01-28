import webstomp, {Client, Frame, Message, Subscription} from 'webstomp-client'
import {Store} from "redux"
import {TERMINAL_RECEIVE} from "../terminal/TerminalReducer"
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_USER_CONNECTION} from "./GenericServerActionProcessor"
import {currentUser} from "../user/CurrentUser"
import {notify} from "../util/Notification";
import {NAVIGATE_PAGE, Page} from "../menu/pageReducer";


export const WS_UNRESTRICTED = "WS_UNRESTRICTED"
export const WS_HACKER_MAIN = "WS_HACKER_MAIN"
export const WS_NETWORK_APP = "WS_NETWORK_APP"

export type ConnectionType = "WS_UNRESTRICTED" | "WS_HACKER_MAIN" | "WS_NETWORK_APP"

const pathByConnectionType = { WS_UNRESTRICTED: "/ws_unrestricted", WS_HACKER_MAIN: "/ws_hacker", WS_NETWORK_APP: "/ws_networked_app" }

let connectionIdCount = 0

export class WebSocketConnection {

    client: Client = null as unknown as Client
    developmentServer: boolean = false
    store: Store = null as unknown as Store

    waitingForType: string | null = null
    waitingIgnoreList: string[] | null = []

    subscriptions: {[key: string] : Subscription} = {}

    actions: { [key: string]: (action: any) => void } = {}

    connectionType: ConnectionType = null as unknown as ConnectionType
    connectionId: string = null as unknown as string

    create(connectionType: ConnectionType, store: Store, additionalOnWsOpen: () => void, waitForType: string | null = null) {
        this.connectionType = connectionType
        this.store = store
        const url = this.createUrl(connectionType)
        this.client = webstomp.client(url, {debug: false, heartbeat: {incoming: 0, outgoing: 0}})

        if (waitForType) {
            this.waitFor(waitForType, null)
        }

        this.client.connect({}, (event) => {
            this.onWsOpen(event!, additionalOnWsOpen)
        }, (event) => {
            this.onWsConnectError(event)
        })
    }

    createUrl(connection: ConnectionType) {
        const hostName: string = window.location.hostname
        this.developmentServer = (window.location.port === "3000")
        // During development connect directly to backend, websocket proxying does not work with create-react-app.
        const port = this.developmentServer ? "80" :  window.location.port
        const protocol = (window.location.protocol === "http:") ? "ws" : "wss"
        const path = pathByConnectionType[connection]
        return `${protocol}://${hostName}:${port}${path}`
    }

    onWsOpen(event: Frame, additionalOnWsOpen: () => void) {
        const userIdAndConnection = event.headers["user-name"]!!

        if (userIdAndConnection === "login-needed") {
            this.redirectToLogin()
            return
        }

        const [userId, connectionId ] = userIdAndConnection.split("_")
        if ( connectionId !== undefined) {
            currentUser.id = userId
            this.connectionId = connectionId
        }
        else {
            this.connectionId = userIdAndConnection
        }

        this.store.dispatch({type: TERMINAL_RECEIVE, data: "Logged in as [info]" + userId, terminalId: "main"})

        this.setupHeartbeat()
        this.subscribe('/user/reply') /// This will receive messages for this specific connection
        this.subscribe(`/topic/user/${userId}`) /// messages for this user on all connections
        this.subscribe(`/topic/user/${userIdAndConnection}`) /// messages for this user connection specifically (/reply does not work)

        setTimeout(additionalOnWsOpen, 100) // give the server some time to process the subscriptions before sending additional messages.
    }

    redirectToLogin() {
        window.location.href = `/redirectToLogin?next=${document.location.pathname}`
    }

    onWsConnectError(event: CloseEvent | Frame) {
        if (event instanceof CloseEvent) {
            console.error("Connect failed, code: " + event.code)
            if (event.code === 1006) {
                notify({type: "fatal", message: "Failed to establish connection to AV server. It might not be running." })
                return
            }
        } else {
            notify({type: "fatal", message: "Failed to establish connection to AV server." })
            console.error("Connection failed: " + event.toString())
        }

        this.store.dispatch({type: SERVER_DISCONNECT})
        if (this.actions[SERVER_DISCONNECT]) {
            this.actions[SERVER_DISCONNECT]({})
        }
    }

    setupHeartbeat() {
        // don't set up heartbeat for development server, as the server will not disconnect anyway,
        // and heartbeat messages wil clutter other messages.
        if (!this.developmentServer) {
            // Our Server does not support server side heartbeats
            // because we are using Simple Broker, not full-blown RabitMQ or the like.
            // so we use this home brew alternative
            setInterval(() => {
                this.client.send("/hb", "")
            }, 1000 * 15)
        }
    }

    subscribe(path: string) {
        const existingSubscription = this.subscriptions[path]
        if ( existingSubscription) {
            existingSubscription.unsubscribe()
        }

        const id = this.makeId()
        const subscription = this.client.subscribe(path, (wsMessage) => {
            this.handleEvent(wsMessage)
        }, { id: id})
        this.subscriptions[path] = subscription
    }

    makeId(): string {
        connectionIdCount ++
        if (currentUser.isSet()) {
            return `${currentUser.id}_${this.connectionId}_${connectionIdCount}`
        }
        const random = Math.floor(Math.random() * 1000000)
        return `${random}_${this.connectionId}_${connectionIdCount}`
    }

    unsubscribe(path: string) {
        const existingSubscription = this.subscriptions[path]
        if ( !existingSubscription) {
            throw Error( `Subscription not found for: ${path}`)
        }
        existingSubscription.unsubscribe()
        delete this.subscriptions[path]
    }

    handleEvent(wsMessage: Message) {
        const action = JSON.parse(wsMessage.body)

        if (action.type === SERVER_USER_CONNECTION &&
            this.connectionType === action.data.type &&
            this.connectionId !== action.data.connectionId) {

            this.abort("Another browser tab was opened for hacking. \n\n Close or refresh this tab.")
        }

        if (this.waitingForType && action.type !== SERVER_ERROR) {
            if (action.type === this.waitingForType) {
                this.waitingForType = null
                this.waitingIgnoreList = []
            } else {
                if (this.waitingIgnoreList === null || this.waitingIgnoreList.includes(action.type)) {
                    return
                }
            }
        }

        this.store.dispatch(action)

        const actionMethod = this.actions[action.type]
        if (actionMethod) {
            actionMethod(action.data)
        }

        /* Our server (Spring simple broker) does not support ACK , so we keep our network logs clean */
        // wsMessage.ack()
    }

    subscribeForRun(runId: string, siteId: string) {
        this.subscribe(`/topic/run/${runId}`)
        this.subscribe(`/topic/site/${siteId}`)
    }

    unsubscribeForRun(runId: string | null, siteId: string | null) {
        this.unsubscribe(`/topic/run/${runId}`)
        this.unsubscribe(`/topic/site/${siteId}`)
    }



    sendObject(path: string, payload: any) {
        let data = JSON.stringify(payload)
        this.send(path, data)
    }

    sendObjectWithRunId(path: string, payload: any) {
        payload.runId = this.store.getState().run.run.runId
        this.sendObject(path, payload)
    }

    send(path: string, data: string | any) {
        const payload = (typeof data === 'object') ? JSON.stringify(data) : data
        this.client.send(`/av${path}`, payload)
    }

    sendWhenReady(path: string, data: string | any) {
        if (this.client.connected) {
            this.send(path, data)
        }
        else {
            setTimeout(() => {
                this.sendWhenReady(path, data)
            }, 10)
        }
    }

    /** Ignore certain actions until an action with a specific type is received.
     * For example: make sure we get the init scan event before we start parsing changes to the site.
     * if ignoreList is null, then all events are ignored that are not the specified type.*/
    waitFor(type: string, ignoreList: string[] | null) {
        this.waitingForType = type
        this.waitingIgnoreList = ignoreList
    }

    abort(message: string) {
        this.store.dispatch({type: NAVIGATE_PAGE, to: Page.FORCE_DISCONNECT})
        this.client.disconnect()
        notify({type: "fatal", message, title: "ERROR"})
    }

    addAction(actionName: string, actionMethod: (action: any) => void) {
        // if (this.actions[actionName]) {
        //     throw Error("Duplicate action definition for : " + actionName)
        // }
        this.actions[actionName] = actionMethod
    }
}

export const webSocketConnection = new WebSocketConnection()
