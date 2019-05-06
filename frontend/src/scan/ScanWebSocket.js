import webstomp from 'webstomp-client';
import {notify, notify_fatal} from "../common/Notification";
import {TERMINAL_RECEIVE} from "../common/terminal/TerminalActions";
import {SERVER_SCAN_FULL} from "./ScanActions";
import {SERVER_FATAL, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION, SET_USER_ID} from "../common/CommonActions";
import {orderByDistance} from "./lib/NodeDistance";

let initWebSocket = (store, scanId, siteId, callback, dispatch) => {

    let siteInitializedFromServer = false;

    let port = window.location.port;
    let hostName = window.location.hostname;
    let developmentServer = (port === "3000");
    // During development connect directly to backend, websocket proxying does not work with create-react-app.
    port = developmentServer ? "80" : port;
    let protocol = (window.location.protocol === "http:") ? "ws" : "wss";
    let url = protocol + "://" + hostName + ":" + port + "/attack_vector_websocket";


    let client = webstomp.client(url, {debug:false, heartbeat: {incoming: 0, outgoing: 0}});

    let onWsOpen = (event) => {
        let userId = event.headers["user-name"];
        // notify_neutral('Status','Connection with server established (' + userName + ")");
        dispatch({type: TERMINAL_RECEIVE, data: "Logged in as [info]" + userId });
        dispatch({type: SET_USER_ID, userId: userId});

        setupHeartbeat(developmentServer, client);
        client.subscribe('/topic/scan/' + scanId, handleEvent);
        client.subscribe('/topic/site/' + siteId, handleEvent);
        client.subscribe('/user/reply', handleEvent );
        client.subscribe('/user/error', handleServerError );
        callback(true);
    };

    const onWsConnectError = (event) => {
        notify_fatal('Connection with server lost. Please refresh browser.');
        dispatch({type: TERMINAL_RECEIVE, data: "[b warn]Connection with server lost. Please refresh browser." });
        // callback(false);
    };

    const handleEvent = (wsMessage) => {
        const body = JSON.parse(wsMessage.body);

        if ( body.type === SERVER_FATAL) {
            notify_fatal(body.data);
            let event = {...body, globalState: store.getState()};
            store.dispatch(event);
            return;
        }

        if (body.type === SERVER_NOTIFICATION) {
            notify(body.data);
            return;
        }

        if (body.type === SERVER_FORCE_DISCONNECT) {
            notify(body.data);
            callback(false);
        }

        if (!siteInitializedFromServer) {
            if (body.type === SERVER_SCAN_FULL) {
                siteInitializedFromServer = true;
            }
            else {
                console.log("Ignored event occurring before site initialization: " + body.type);
                return;
            }
        }

        if (body.type === SERVER_SCAN_FULL) {
            orderByDistance(body.data);
        }

        let event = {...body, globalState: store.getState()};
        store.dispatch(event);
    };

    const handleServerError = (wsMessage) => {
        let body = JSON.parse(wsMessage.body);
        notify(body);
    };

    const setupHeartbeat = (developmentServer, client) => {
        // don't set up heartbeat for development server, as the server will not disconnect anyway,
        // and heartbeat messages wil clutter other messages.
        if (!developmentServer) {
            // Our Server does not support server side heartbeats
            // because we are using Simple Broker, not full blown RabitMQ or the like.
            // so we use this home brew alternative
            setInterval(() => {
                client.send("/hb", "");
            }, 1000 * 15);
        }
    };

    client.connect({}, onWsOpen, onWsConnectError);

    return client;
};

export default initWebSocket