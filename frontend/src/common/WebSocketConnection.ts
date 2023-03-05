import webstomp, {Client, Frame, Message, Subscription} from 'webstomp-client'
import {notify} from "./Notification"
import {Store} from "redux"
import {TERMINAL_RECEIVE} from "./terminal/TerminalReducer"
import {SET_USER_ID} from "./reducer/UserIdReducer"
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT} from "../hacker/server/GenericServerActionProcessor"
import {currentUser} from "./CurrentUser"


export const WEBSOCKET_RUN = "run_ws"
export const WEBSOCKET_ICE = "ice_ws"

export class WebSocketConnection {

    client: Client = null as unknown as Client
    developmentServer: boolean = false
    store: Store = null as unknown as Store

    waitingForType: string | null = null
    waitingIgnoreList: string[] | null = []

    subscriptions: Subscription[] = []

    actions: { [key: string]: (action: any) => void } = {}

    constructor() {

    }

    create(endpoint: string, store: Store, additionalOnWsOpen: () => void, waitForType: string | null = null) {
        this.store = store
        const url = this.createUrl(endpoint)
        this.client = webstomp.client(url, {debug: false, heartbeat: {incoming: 0, outgoing: 0}})

        if (waitForType) {
            this.waitFor(waitForType, null)
        }

        this.client.connect({"testHeader": "testValue"}, (event) => {
            this.onWsOpen(event!, additionalOnWsOpen)
        }, (event) => {
            this.onWsConnectError(event)
        })
    }

    createUrl(endpoint: string) {
        let port: string = window.location.port
        const hostName: string = window.location.hostname
        this.developmentServer = (port === "3000")
        // During development connect directly to backend, websocket proxying does not work with create-react-app.
        port = this.developmentServer ? "80" : port
        const protocol = (window.location.protocol === "http:") ? "ws" : "wss"
        return `${protocol}://${hostName}:${port}/${endpoint}`
    }

    onWsOpen(event: Frame, additionalOnWsOpen: () => void) {
        const userIdAndConnection = event.headers["user-name"]!!
        const userId = userIdAndConnection.substring(0, userIdAndConnection.indexOf(":"))
        const connectionId = userIdAndConnection.substring(userId.length + 1)
        currentUser.id = userId
        currentUser.connectionId = connectionId

        this.store.dispatch({type: TERMINAL_RECEIVE, data: "Logged in as [info]" + userId, terminalId: "main"})
        this.store.dispatch({type: SET_USER_ID, userId: userId})

        this.setupHeartbeat()
        this.subscribe('/user/reply', false) /// This will receive messages for this specific connection
        this.subscribe(`/topic/user/${userId}`, false) /// messages for this user on all connections
        additionalOnWsOpen()
    }

    onWsConnectError(event: CloseEvent | Frame) {
        if (event instanceof CloseEvent) {
            console.error("Connect failed: " + event.reason)
        } else {
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
            // because we are using Simple Broker, not full blown RabitMQ or the like.
            // so we use this home brew alternative
            setInterval(() => {
                this.client.send("/hb", "")
            }, 1000 * 15)
        }
    }


    subscribe(path: string, canUnsubscribe: boolean = false) {
        const subscription = this.client.subscribe(path, (wsMessage) => {
            this.handleEvent(wsMessage)
        })
        if (canUnsubscribe) {
            this.subscriptions.push(subscription)
        }
    }

    handleEvent(wsMessage: Message) {
        const action = JSON.parse(wsMessage.body)

        if (action.type === SERVER_FORCE_DISCONNECT) {
            this.client.disconnect()
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
        this.subscribe('/topic/run/' + runId, true)
        this.subscribe('/topic/site/' + siteId, true)
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
        this.client.send(path, payload)
    }

    /** Ignore certain actions until an action with a specific type is received.
     * For example: make sure we get the init scan event before we start parsing changes to the site.
     * if ignoreList is null, then all events are ignored that are not the specified type.*/
    waitFor(type: string, ignoreList: string[] | null) {
        this.waitingForType = type
        this.waitingIgnoreList = ignoreList
    }

    unsubscribe() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe()
        })
        this.subscriptions = []
    }

    abort() {
        this.client.disconnect()
    }

    addAction(actionName: string, actionMethod: (action: any) => void) {
        if (this.actions[actionName]) {
            throw Error("Duplicate action definition for : " + actionName)
        }
        this.actions[actionName] = actionMethod
    }
}

export const webSocketConnection = new WebSocketConnection()
