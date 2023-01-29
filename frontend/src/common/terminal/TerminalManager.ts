import {ENTER_KEY, F12_KEY, F2_KEY} from "../../KeyCodes";
import {TERMINAL_KEY_PRESS, TERMINAL_TICK} from "./TerminalActions";
import {delay} from "../Util";
import {Dispatch, Store} from "redux";
import Terminal from "./Terminal";

class TerminalManager {

    store: Store = null as unknown as Store
    dispatch = null as unknown as Dispatch
    running: boolean = false

    terminals: {[key: string]: Terminal} = {};

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        // this.terminalTickIntervalId =
        delay(() => {

            setInterval(() => {
                this.dispatch({type: TERMINAL_TICK});
            }, 50);
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
            this.terminals[terminalId].submit(key);
        } else {
            this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: terminalId});
        }
    }

    registerTerminal(terminalId: string, terminal: Terminal) {
        this.terminals[terminalId] = terminal;
    }
}

export const terminalManager = new TerminalManager();
