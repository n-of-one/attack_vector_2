import webstomp, {Client, Frame, Message, Subscription} from 'webstomp-client';
import {notify} from "./Notification";
import {Store} from "redux";
import {ActionType} from "./Util";
import {TERMINAL_RECEIVE} from "./terminal/TerminalReducer";
import {SET_USER_ID} from "./reducer/UserIdReducer";
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT} from "../hacker/server/GenericServerActionProcessor";

export class WebSocketConnection {

    client: Client = null as unknown as Client
    url: string
    developmentServer: boolean
    store: Store = null as unknown as Store

    waitingForType: string | null = null
    waitingIgnoreList: string[] | null = [];

    subscriptions: Subscription[] = [];

    actions: {[key: string]: (action: ActionType) => void} = {};

    constructor() {
        let port: string = window.location.port;
        const hostName: string = window.location.hostname;
        this.developmentServer = (port === "3000");
        // During development connect directly to backend, websocket proxying does not work with create-react-app.
        port = this.developmentServer ? "80" : port;
        const protocol = (window.location.protocol === "http:") ? "ws" : "wss";
        this.url = protocol + "://" + hostName + ":" + port + "/attack_vector_websocket";

    }

    create(store: Store, additionalOnWsOpen: () => void, waitForType: string | null = null) {
        this.store = store;
        this.client = webstomp.client(this.url, {debug: false, heartbeat: {incoming: 0, outgoing: 0}});

        if (waitForType) {
            this.waitFor(waitForType, null);
        }

        this.client.connect({}, (event) => {
            this.onWsOpen(event!, additionalOnWsOpen);
        }, (event) => {
            this.onWsConnectError(event);
        });
    }

    onWsOpen(event: Frame, additionalOnWsOpen: () => void) {
        const userId = event.headers["user-name"];
        if (!userId || userId === "error") {
            notify ({type: "fatal", message:"Please close this browser tab, hackers can only use one browser tab at a time.."});
            return
        }

        // notify_neutral('Status','Connection with server established (' + userName + ")");
        this.store.dispatch({type: TERMINAL_RECEIVE, data: "Logged in as [info]" + userId, terminalId: "main"});
        this.store.dispatch({type: SET_USER_ID, userId: userId});

        this.setupHeartbeat();
        this.subscribe('/user/reply', false);
        additionalOnWsOpen();
    }

    onWsConnectError(event: CloseEvent | Frame) {
        if ( event instanceof CloseEvent) {
            console.error("Connect failed: "+ event.reason)
        }
        else {
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
                this.client.send("/hb", "");
            }, 1000 * 15);
        }
    }


    subscribe(path: string, canUnsubscribe: boolean = false) {
        const subscription = this.client.subscribe(path, (wsMessage) => {
            this.handleEvent(wsMessage);
        });
        if (canUnsubscribe) {
            this.subscriptions.push(subscription);
        }
    }

    handleEvent(wsMessage: Message) {
        const action = JSON.parse(wsMessage.body);

        if (action.type === SERVER_FORCE_DISCONNECT) {
            this.client.disconnect();
        }

        if (this.waitingForType && action.type !== SERVER_ERROR) {
            if (action.type === this.waitingForType) {
                this.waitingForType = null;
                this.waitingIgnoreList = [];
            }
            else {
                if (this.waitingIgnoreList === null || this.waitingIgnoreList.includes(action.type)) {
                    return
                }
            }
        }

        this.store.dispatch(action);

        const actionMethod = this.actions[action.type]
        if (actionMethod) {
            actionMethod(action.data);
        }

        /* Our server (Spring simple broker) does not support ACK , so we keep our network logs clean */
        // wsMessage.ack();
    };

    subscribeForRun(runId: string, siteId: string) {
        this.subscribe('/topic/run/' + runId, true);
        this.subscribe('/topic/site/' + siteId, true);
    }

    sendObject(path: string, payload: ActionType) {
        let data = JSON.stringify(payload);
        this.send(path, data);
    }

    sendObjectWithRunId(path: string, payload: ActionType) {
        payload.runId = this.store.getState().run.run.runId
        this.sendObject(path, payload)
    }

    send(path: string, data: string | ActionType) {
        const payload =  (typeof data === 'object') ? JSON.stringify(data) : data
        this.client.send(path, payload);
    }

    /** Ignore certain actions until an action with a specific type is received.
     * For example: make sure we get the init scan event before we start parsing changes to the site.
     * if ignoreList is null, then all events are ignored that are not the specified type.*/
    waitFor(type: string, ignoreList: string[] | null) {
        this.waitingForType = type;
        this.waitingIgnoreList = ignoreList;
    }

    unsubscribe() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
        this.subscriptions = [];
    }

    abort() {
        this.client.disconnect();
    }

    addAction(actionName: string, actionMethod: (action: ActionType) => void) {
        this.actions[actionName] = actionMethod;
    }
}

export const webSocketConnection = new WebSocketConnection();
