import webstomp from 'webstomp-client';
import {notify, notify_fatal, notify_neutral} from "../common/Notification";
import {SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "./EditorActions";

let initWebSocket = (store, siteId, callback) => {

    let port = window.location.port;
    let hostName = window.location.hostname;
    let developmentServer = (port === "3000");
    // During development connect directly to backend, websocket proxying does not work with create-react-app.
    port = developmentServer ? "80" : port;
    let protocol = (window.location.protocol === "http:") ? "ws" : "wss";
    let url = protocol + "://" + hostName + ":" + port + "/ws";
    let client = webstomp.client(url, {debug:false, heartbeat: {incoming: 0, outgoing: 0}});

    let onWsOpen = (event) => {
        let userName = event.headers["user-name"];
        notify_neutral('Status','Connection with Mainframe established (' + userName + ")");


        client.subscribe('/topic/site/' + siteId, handleSiteEvent);
        client.subscribe('/user/reply', handleUserMessge );
        client.subscribe('/user/error', handleServerError );
        callback(true);

        // don't set up heartbeat for development server, as it will not disconnect,
        // and heartbeat messages wil clutter other messages.
        if (!developmentServer) {

            // Server side heartbeat not supported (Simple Broker, not full blown RabitMQ or the like)
            setInterval(() => {
                client.send("/hb", "");
            }, 1000 * 15);
        }
    };

    const onWsConnectError = () => {
        notify_fatal('Connection with mainframe lost. Please refresh browser.');
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

    client.connect({}, onWsOpen, onWsConnectError);

    return client;
};

export default initWebSocket