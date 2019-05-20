import {ENTER_KEY, F12_KEY, F2_KEY} from "../../KeyCodes";
import {TERMINAL_KEY_PRESS, TERMINAL_SUBMIT, TERMINAL_TICK} from "./TerminalActions";

class TerminalManager {

    store = null;
    dispatch = null;
    terminalTickIntervalId = null;
    running = false;

    constructor() {
    }

    init(store) {
        this.store = store;
        this.dispatch = store.dispatch;
        this.terminalTickIntervalId = setInterval(() => {
            if (this.running) {
                this.dispatch({type: TERMINAL_TICK});
            }
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
        if (keyCode === ENTER_KEY) {
            this.dispatch({type: TERMINAL_SUBMIT, key: key, command: this.store.getState().terminal.input, terminalId: "main"});
        }
        else {
            this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: "main"});
        }
    }
}

const terminalManager = new TerminalManager();

export default terminalManager;