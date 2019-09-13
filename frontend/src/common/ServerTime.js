/**
 * The Javascript app will receive time events from the server. The client can run on a platform that has a different internal time than the server's time.
 *
 * To counteract this, the client application will only use 'server time'. The server will tell the client what its time is during the first connection
 * and during keep-alive calls.
 *
 * NOTE: Do not use: Date.now() OR new Date() instead use: serverTime.now()
 */
import {zeroPad} from "./component/Pad";

class ServerTime {

    timeDifference = null;

    init(serverTime) {
        const clientSync = Date.now();
        const serverSync = new Date(serverTime).getTime();

        this.timeDifference = serverSync - clientSync;
    }

    now() {
        return Date.now() + this.timeDifference
    }

    secondsLeft(serverTime) {
        const nowMillis = this.now();
        const serverMillis = new Date(serverTime).getTime();

        return Math.ceil((serverMillis - nowMillis)/1000);
    }

    format(totalSecondsLeft) {
        const waitHours = Math.floor(totalSecondsLeft / (60 * 60));
        const secondsLeftForMinutes = totalSecondsLeft % (60 * 60);
        const waitMinutes = Math.floor(secondsLeftForMinutes / 60);
        const waitSeconds = secondsLeftForMinutes % 60;

        return zeroPad(waitHours, 2) + ":" + zeroPad(waitMinutes, 2) + ":" + zeroPad(waitSeconds, 2);
    }
}

const serverTime = new ServerTime();
export default serverTime;