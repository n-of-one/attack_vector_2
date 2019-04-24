/**
 * Thread is a utility for scheduling javascript events.
 *
 * An event can set a wait time (in 50ms units). The next event will wait that time before firing.
 *
 * The event loop will auto-clear if there are no events in the queue, no need to externally stop/clear the interval.
 */

export default class Thread {

    queue = [];

    /** Time in millis at which time the wait time is over and the next event can be started. */
    waitEnd = null;

    /** The interval-id of the main loop thread.
     * If it is null, then there is no current main loop, and a new one needs to be started */
    intervalId = null;

    constructor(term) {
        this.term = term;
        this.queue = [];
    }

    run(wait, functionToRun) {
        let that = this;
        this._schedule( () => {
            functionToRun();
            that._setWait(wait);
        });
    }

    runWithThis(wait, functionToRun, withThis) {
        let that = this;
        this._schedule( () => {
            functionToRun.bind(withThis)();
            that._setWait(wait);
        });
    }

    wait(wait) {
        this.run(wait, function () {
        });
    }


    _schedule(event) {
        if (this.intervalId === null) {
            const that = this;
            this.intervalId = setInterval( () => { that._mainLoop(); }, 50);
        }
        this.queue.push(event);
    }

    _setWait(wait) {
        this.waitEnd = Date.now() + 50 * wait;
    }

    /** Main loop tick function. Triggers every 100ms. */
    _mainLoop() {
        if (this.waitEnd != null) {
            if (Date.now() < this.waitEnd) {
                return;
            }
            this.waitEnd = null;
        }

        if (this.queue.length > 0) {
            const event = (this.queue.splice(0, 1))[0];
            event();
        }
        else {
            /* Thread cleans up interval if there is no more event in the queue. */
            clearInterval(this.intervalId);
        }
    }
}