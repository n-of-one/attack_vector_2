import {notify_fatal} from "./Notification";

let assertNotNullUndef = (toCheck, debugInfo) => {
    if (toCheck === null || toCheck === undefined) {
        assertFail(debugInfo);
    }
};

let assertFail = (debugInfo) => {
    let stack = new Error().stack;
    let debugString = JSON.stringify(debugInfo);
    console.error("Assert failed: value was null or undefined. Debug info: " + debugString, stack);
    notify_fatal('Internal error, please refresh browser.');
};

export { assertNotNullUndef, assertFail }