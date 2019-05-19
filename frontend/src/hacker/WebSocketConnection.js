import webstomp from 'webstomp-client';
import {notify, notify_fatal} from "../common/Notification";
import {TERMINAL_RECEIVE} from "../common/terminal/TerminalActions";
import {SERVER_SCAN_FULL} from "./scan/model/ScanActions";
import {NAVIGATE_PAGE, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION, SET_USER_ID} from "../common/enums/CommonActions";
import {orderByDistance} from "./scan/lib/NodeDistance";
import {MAIL} from "./HackerPages";

class WebSocketConnection {

    client = null;
    url = null;
    developmentServer = false;
    store = null;

    constructor() {
        let port = window.location.port;
        let hostName = window.location.hostname;
        this.developmentServer = (port === "3000");
        // During development connect directly to backend, websocket proxying does not work with create-react-app.
        port = this.developmentServer ? "80" : port;
        let protocol = (window.location.protocol === "http:") ? "ws" : "wss";
        this.url = protocol + "://" + hostName + ":" + port + "/attack_vector_websocket";

    }

    create(store) {
        //FIXME: initialization must be part of the redux state.
        // let siteInitializedFromServer = false;

        this.store = store;
        this.client = webstomp.client(this.url, {debug: false, heartbeat: {incoming: 0, outgoing: 0}});
        this.client.connect({}, (event) => {
            this.onWsOpen(event);
        }, () => {
            this.onWsConnectError();
        });
    }

    onWsOpen(event) {
        let userId = event.headers["user-name"];
        // notify_neutral('Status','Connection with server established (' + userName + ")");
        this.dispatch({type: TERMINAL_RECEIVE, data: "Logged in as [info]" + userId, terminalId: "main"});
        this.dispatch({type: SET_USER_ID, userId: userId});

        this.setupHeartbeat();
        this.subscribe('/user/reply');
    }


    onWsConnectError(event) {
        notify_fatal('Connection with server lost. Please refresh browser.');
        this.dispatch({type: TERMINAL_RECEIVE, data: "[b warn]Connection with server lost. Please refresh browser."});
        // callback(false);
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


    subscribe(path) {
        this.client.subscribe(path, (wsMessage) => {
            this.handleEvent(wsMessage);
        });
    }

    handleEvent(wsMessage) {
        const body = JSON.parse(wsMessage.body);

        if (body.type === SERVER_ERROR) {
            this.dispatch(body);

            notify_fatal(body.data.message);
            this.client.disconnect();
            return;
        }

        if (body.type === SERVER_NOTIFICATION) {
            notify(body.data);
            return;
        }

        if (body.type === SERVER_FORCE_DISCONNECT) {
            notify(body.data);
            // FIXME: enum
            this.dispatch({type: "SERVER_DISCONNECTED"});
        }

        //FIXME: initialization must be part of the redux state.
        // if (!siteInitializedFromServer) {
        //     if (body.type === SERVER_SCAN_FULL) {
        //         siteInitializedFromServer = true;
        //     }
        //     else {
        //         console.log("Ignored event occurring before site initialization: " + body.type);
        //         return;
        //     }
        // }

        if (body.type === SERVER_SCAN_FULL) {
            orderByDistance(body.data);
        }

        this.store.dispatch(body);
    };

    dispatch(body) {
        let event = {...body, globalState: this.store.getState()};
        this.store.dispatch(event);
    }

    subScribeForScan(scanId, siteId) {
        this.subscribe('/topic/scan/' + scanId);
        this.subscribe('/topic/site/' + siteId);
    }

    send(path, data) {
        this.client.send(path, data);
    }

}

const webSocketConnection = new WebSocketConnection();

export default webSocketConnection;
