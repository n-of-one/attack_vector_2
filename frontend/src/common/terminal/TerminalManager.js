import {ENTER_KEY, F12_KEY, F2_KEY} from "../../KeyCodes";
import {TERMINAL_KEY_PRESS, TERMINAL_SUBMIT, TERMINAL_TICK} from "./TerminalActions";

export default class TerminalManager {

    store = null;
    dispatch = null;

    constructor(store) {
        this.store = store;
        this.dispatch = store.dispatch;
    }

    start() {
        setInterval(() => {
            this.dispatch({type: TERMINAL_TICK});
        }, 10);

        window.onkeydown= (event) => {
            this.handleKeyDown(event);
        }
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
