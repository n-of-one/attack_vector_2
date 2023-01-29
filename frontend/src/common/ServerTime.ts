/**
 * The Javascript app will receive time events from the server. The client can run on a platform that has a different internal time than the server's time.
 *
 * To counteract this, the client application will only use 'server time'. The server will tell the client what its time is during the first connection
 * and during keep-alive calls.
 *
 * NOTE: Do not use: Date.now() OR new Date() instead use: serverTime.now()
 */

class ServerTime {

    timeDifference: number | null = null

    init(serverTime: string) {
        const clientSync = Date.now();
        const serverSync = new Date(serverTime).getTime();

        this.timeDifference = serverSync - clientSync;
    }

    now() {
        if (!this.timeDifference) {
            return Date.now() // not initialized yet, assume 0 difference
        }

        return Date.now() + this.timeDifference
    }

    secondsLeft(serverTime: string) {
        const nowMillis = this.now();
        const serverMillis = new Date(serverTime).getTime();

        return Math.ceil((serverMillis - nowMillis) / 1000);
    }

}

export const serverTime = new ServerTime();
