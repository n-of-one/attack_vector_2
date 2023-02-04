import {ENTER_KEY, F12_KEY, F2_KEY} from "../../KeyCodes";
import {delay} from "../Util";
import {Dispatch, Store} from "redux";
import {TERMINAL_KEY_PRESS, TERMINAL_TICK} from "./TerminalReducer";
import {TICK_MILLIS} from "../Schedule";

class TerminalManager {

    store: Store = null as unknown as Store
    dispatch = null as unknown as Dispatch
    running: boolean = false

    terminalSubmit: {[key: string]: () => void } = {}

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        delay(() => {

            setInterval(() => {
                this.dispatch({type: TERMINAL_TICK});
            }, TICK_MILLIS);
        })

        window.onkeydown = (event: KeyboardEvent) => {
            if (this.running) {
                this.handleKeyDown(event);
            }
        }
    }

    start() {
        this.running = true;
    }

    stop() {
        this.running = false;
    }

    handleKeyDown(event: KeyboardEvent) {
        let {keyCode, key} = event;
        if (keyCode >= F2_KEY && keyCode <= F12_KEY) {
            return;
        }

        event.preventDefault();

        const terminalId = this.store.getState().activeTerminalId;

        if (keyCode === ENTER_KEY) {
            const submit = this.terminalSubmit[terminalId]
            if (submit) { submit() }
        } else {
            this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: terminalId});
        }
    }

    registerTerminalSubmit(terminalId: string, submit: () => void) {
        this.terminalSubmit[terminalId] = submit
    }
}

export const terminalManager = new TerminalManager()
