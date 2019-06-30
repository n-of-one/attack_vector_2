import {ENTER_KEY, F12_KEY, F2_KEY} from "../../KeyCodes";
import {TERMINAL_KEY_PRESS, TERMINAL_SUBMIT, TERMINAL_TICK} from "./TerminalActions";

class TerminalManager {

    store = null;
    dispatch = null;
    terminalTickIntervalId = null;
    running = false;

    terminals = {};

    init(store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.terminalTickIntervalId = setInterval(() => {
            this.dispatch({type: TERMINAL_TICK});
        }, 10);

        window.onkeydown = (event) => {
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

    handleKeyDown(event) {
        let {keyCode, key} = event;
        if (keyCode >= F2_KEY && keyCode <= F12_KEY) {
            return;
        }

        event.preventDefault();

        const terminalId = this.store.getState().activeTerminalId;

        if (keyCode === ENTER_KEY) {
            this.terminals[terminalId].submit(key);
        }
        else {
            this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: terminalId});
        }
    }

    registerTerminal(terminalId, terminal) {
        this.terminals[terminalId] = terminal;
    }

}

const terminalManager = new TerminalManager();

export default terminalManager;