import webstomp from 'webstomp-client';
import {TERMINAL_RECEIVE} from "../common/terminal/TerminalActions";
import {SERVER_SCAN_FULL} from "./scan/model/ScanActions";
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SET_USER_ID} from "../common/enums/CommonActions";
import {orderByDistance} from "./scan/lib/NodeDistance";

class WebSocketConnection {

    client = null;
    url = null;
    developmentServer = false;
    store = null;

    waitingForType = null;
    waitingIgnoreList = [];

    subscriptions = [];

    constructor() {
        let port = window.location.port;
        let hostName = window.location.hostname;
        this.developmentServer = (port === "3000");
        // During development connect directly to backend, websocket proxying does not work with create-react-app.
        port = this.developmentServer ? "80" : port;
        let protocol = (window.location.protocol === "http:") ? "ws" : "wss";
        this.url = protocol + "://" + hostName + ":" + port + "/attack_vector_websocket";

    }

    create(store, additionalOnWsOpen) {
        this.store = store;
        this.client = webstomp.client(this.url, {debug: false, heartbeat: {incoming: 0, outgoing: 0}});
        this.client.connect({}, (event) => {
            this.onWsOpen(event, additionalOnWsOpen);
        }, () => {
            this.onWsConnectError();
        });
    }

    onWsOpen(event, additionalOnWsOpen) {
        let userId = event.headers["user-name"];
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

        if (this.waitingForType) {
            if (action.type === this.waitingForType) {
                this.waitingForType = null;
                this.waitingIgnoreList = [];
            }
            else {
                if (this.waitingIgnoreList.includes(action.type)) {
                    return
                }
            }
        }

        // FIXME: do this server side
        if (action.type === SERVER_SCAN_FULL) {
            orderByDistance(action.data);
        }

        this.store.dispatch(action);

        /* Our server (Spring simple broker) does not support ACK , so we keep our network logs clean */
        // wsMessage.ack();
    };

    dispatch(body) {
        let event = {...body, globalState: this.store.getState()};
        this.store.dispatch(event);
    }

    subscribeForScan(scanId, siteId) {
        this.subscribe('/topic/scan/' + scanId, true);
        this.subscribe('/topic/site/' + siteId, true);
    }

    send(path, data) {
        this.client.send(path, data);
    }

    /** Ignore certain actions until an action with a specific type is received.
     * Make sure we get the init scan event before we start parsing changes to the site */
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
}

const webSocketConnection = new WebSocketConnection();

export default webSocketConnection;
