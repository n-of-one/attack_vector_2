import webstomp from 'webstomp-client';
import {notify, notify_fatal} from "../common/Notification";
import {SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION, SERVER_SITE_FULL} from "../editor/EditorActions";

let initWebSocket = (store, siteId, callback) => {

    let siteInitializedFromServer = false;

    let port = window.location.port;
    let hostName = window.location.hostname;
    let developmentServer = (port === "3000");
    // During development connect directly to backend, websocket proxying does not work with create-react-app.
    port = developmentServer ? "80" : port;
    let protocol = (window.location.protocol === "http:") ? "ws" : "wss";
    let url = protocol + "://" + hostName + ":" + port + "/ws";


    let client = webstomp.client(url, {debug:false, heartbeat: {incoming: 0, outgoing: 0}});

    let onWsOpen = (event) => {
        // let userName = event.headers["user-name"];
        // notify_neutral('Status','Connection with server established (' + userName + ")");

        setupHeartbeat(developmentServer, client);
        client.subscribe('/topic/site/' + siteId, handleSiteEvent);
        client.subscribe('/user/reply', handleUserMessge );
        client.subscribe('/user/error', handleServerError );
        callback(true);
    };

    const onWsConnectError = (event) => {
        notify_fatal('Connection with server lost. Please refresh browser.');
        callback(false);
    };

    const handleSiteEvent = (wsMessage) => {
        const body = JSON.parse(wsMessage.body);
        if (body.type === SERVER_NOTIFICATION) {
            notify(body.data);
            return;
        }

        if (body.type === SERVER_FORCE_DISCONNECT) {
            notify(body.data);
            callback(false);
        }

        if (!siteInitializedFromServer) {
            if (body.type === SERVER_SITE_FULL) {
                siteInitializedFromServer = true;
            }
            else {
                console.log("Ignored event occurring before site initialization: " + body.type);
                return;
            }
        }

        console.log(new Date().getMilliseconds());
        let event = {...body, globalState: store.getState()};
        store.dispatch(event);
    };

    const handleServerError = (wsMessage) => {
        let body = JSON.parse(wsMessage.body);
        notify(body);
    };

    const handleUserMessge = (wsMessage) => {
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