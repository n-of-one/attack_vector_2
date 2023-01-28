import {notify} from "./Notification"
import {webSocketConnection} from "./WebSocketConnection"

let assertNotNullUndef = (toCheck: any, debugInfo: Object) => {
    if (toCheck === null || toCheck === undefined) {
        assertFail(debugInfo)
    }
}

const assertFail = (debugInfo: Object) => {
    let stack = new Error().stack
    let debugString = JSON.stringify(debugInfo)
    console.error("Assert failed: value was null or undefined. Debug info: " + debugString, stack)
    notify({type: 'fatal', message: 'Internal error, please refresh browser.\n(see browser log for details)'})
    webSocketConnection.abort()
}

export { assertNotNullUndef, assertFail }