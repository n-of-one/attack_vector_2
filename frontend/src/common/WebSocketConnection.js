import webstomp from 'webstomp-client';
import {TERMINAL_RECEIVE} from "./terminal/TerminalActions";
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SET_USER_ID} from "./enums/CommonActions";
import {notify_fatal} from "./Notification";

class WebSocketConnection {

    client = null;
    url = null;
    developmentServer = false;
    store = null;

    waitingForType = null;
    waitingIgnoreList = [];

    subscriptions = [];

    actions = {};

    constructor() {
        let port = window.location.port;
        let hostName = window.location.hostname;
        this.developmentServer = (port === "3000");
        // During development connect directly to backend, websocket proxying does not work with create-react-app.
        port = this.developmentServer ? "80" : port;
        let protocol = (window.location.protocol === "http:") ? "ws" : "wss";
        this.url = protocol + "://" + hostName + ":" + port + "/attack_vector_websocket";

    }

    create(store, additionalOnWsOpen, waitForType) {
        this.store = store;
        this.client = webstomp.client(this.url, {debug: false, heartbeat: {incoming: 0, outgoing: 0}});

        if (waitForType) {
            this.waitFor(waitForType, null);
        }

        this.client.connect({}, (event) => {
            this.onWsOpen(event, additionalOnWsOpen);
        }, (event) => {
            this.onWsConnectError(event);
        });
    }

    onWsOpen(event, additionalOnWsOpen) {
        const userId = event.headers["user-name"];
        if (!userId || userId === "error") {
            notify_fatal("Please close this browser tab, hackers can only use one browser tab at a time..");
            return
        }

        // notify_neutral('Status','Connection with server established (' + userName + ")");
        this.dispatch({type: TERMINAL_RECEIVE, data: "Logged in as [info]" + userId, terminalId: "main"});
        this.dispatch({type: SET_USER_ID, userId: userId});

        this.setupHeartbeat();
        this.subscribe('/user/reply', false);
        additionalOnWsOpen();
    }


    onWsConnectError(event) {
        this.dispatch({type: SERVER_DISCONNECT});
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


    subscribe(path, canUnsubscribe) {
        const subscription = this.client.subscribe(path, (wsMessage) => {
            this.handleEvent(wsMessage);
        });
        if (canUnsubscribe) {
            this.subscriptions.push(subscription);
        }
    }

    handleEvent(wsMessage) {
        const action = JSON.parse(wsMessage.body);

        if (action.type === SERVER_FORCE_DISCONNECT ||
            action.type === SERVER_ERROR) {
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

    dispatch(body) {
        let event = {...body, globalState: this.store.getState()};
        this.store.dispatch(event);
    }

    subscribeForRun(runId, siteId) {
        this.subscribe('/topic/run/' + runId, true);
        this.subscribe('/topic/site/' + siteId, true);
    }

    sendObject(path, payload) {
        let data = JSON.stringify(payload);
        this.send(path, data);
    }

    send(path, data) {
        this.client.send(path, data);
    }

    /** Ignore certain actions until an action with a specific type is received.
     * For example: make sure we get the init scan event before we start parsing changes to the site.
     * if ignoreList is null, then all events are ignored that are not the specified type.*/
    waitFor(type, ignoreList) {
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

    addAction(actionName, actionMethod) {
        this.actions[actionName] = actionMethod;
    }
}

const webSocketConnection = new WebSocketConnection();

export default webSocketConnection;
