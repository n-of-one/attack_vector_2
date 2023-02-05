/**
 * Schedule is a utility for scheduling javascript events.
 *
 * An event can set a wait time (in 50ms units). The next event will wait that time before firing.
 *
 * The 50ms time units correspond to a frame-rate of 20 FPS. This is tied to the TERMINAL_TICK event that is also fired every 50ms.
 * When the TICK and Scheduler use the same time units, it becomes easier to reason about times in the system
 * 20 TICKs per second is already putting some strain on a 2017-high-end-PC, so it is deemed that this is good enough for now.
 *
 * The event loop will auto-clear if there are no events in the queue, no need to externally stop/clear the interval.
 */
import {AnyAction, Dispatch} from "redux";

export const TICK_MILLIS = 50;

export class Schedule {

    queue: Array<() => void> = []

    /** Time in millis at which time the wait time is over and the next event can be started. */
    waitEnd: number | null = null

    /** The interval-id of the main loop schedule.
     * If it is null, then there is no current main loop, and a new one needs to be started */
    intervalId: null | number = null

    active = true

    /** You can give the scheduler the dispatch to allow using the schedule.dispatch(wait, event) function. */
    dispatcher: Dispatch

    constructor(dispatch: Dispatch) {
        this.queue = [];
        this.dispatcher = dispatch;
    }

    run(duration: number, functionToRun: () => void) {
        let that = this;
        this._schedule( () => {
            functionToRun();
            that._setWait(duration);
        });
    }

    dispatch(duration: number, action: AnyAction) {
        let that = this;
        this._schedule( () => {
            this.dispatcher(action);
            that._setWait(duration);
        });
    }

    wait(wait: number) {
        if (!this.active) {
            return;
        }
        this.run(wait, () => {});
    }

    /** clear all activity and prevent schedule from ever working again */
    terminate() {
        this.active = false;
        this.clear();
    }

    /** clear all activity, but allow future activity to continue*/
    clear() {
        this.queue = [];
        this.waitEnd = null;
    }

    _schedule(event: () => void) {
        if (!this.active) {
            return;
        }

        if (this.intervalId === null) {
            const that = this;
            this.intervalId = window.setInterval( () => { that._mainLoop(); }, 5);
        }
        this.queue.push(event);
    }

    _setWait(wait: number) {
        this.waitEnd = Date.now() + TICK_MILLIS * wait;
    }

    /** Main loop tick function. Triggers every 5ms. */
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
            /* Schedule cleans up interval if there is no more event in the queue. */
            window.clearInterval(this.intervalId!);
            this.intervalId = null;
        }
    }
}
