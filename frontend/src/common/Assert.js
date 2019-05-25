import {notify_fatal} from "./Notification";
import webSocketConnection from "./WebSocketConnection";

let assertNotNullUndef = (toCheck, debugInfo) => {
    if (toCheck === null || toCheck === undefined) {
        assertFail(debugInfo);
    }
};

const assertFail = (debugInfo) => {
    let stack = new Error().stack;
    let debugString = JSON.stringify(debugInfo);
    console.error("Assert failed: value was null or undefined. Debug info: " + debugString, stack);
    notify_fatal('Internal error, please refresh browser.\n(see browser log for details)');
    webSocketConnection.abort();
};

export { assertNotNullUndef, assertFail }