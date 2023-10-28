import {ENTER_KEY, F12_KEY, F2_KEY} from "../../KeyCodes";
import {delay} from "../util/Util";
import {Dispatch, Store} from "redux";
import {TERMINAL_KEY_PRESS, TICK, TerminalState} from "./TerminalReducer";
import {ICE_INPUT_TERMINAL_ID, MAIN_TERMINAL_ID} from "./ActiveTerminalIdReducer";

export const TERMINAL_UPDATE_MILLIS = 20 // 50 updates/s

class TerminalManager {

    store: Store = null as unknown as Store
    dispatch = null as unknown as Dispatch
    terminalActive: boolean = false

    terminalSubmit: {[key: string]: () => void } = {}

    init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        delay(() => {
            let currentSecond = Math.floor(Date.now()/1000)

            setInterval(() => {
                const tickSecond =  Math.floor(Date.now() / 1000)
                const isNewSecond = tickSecond !== currentSecond
                currentSecond = tickSecond

                this.dispatch({type: TICK, newSecond: isNewSecond});
            }, TERMINAL_UPDATE_MILLIS);
        })

        window.onkeydown = (event: KeyboardEvent) => {
            if (this.terminalActive) {
                this.handleKeyDown(event);
            }
        }
    }

    start() {
        this.terminalActive = true;
    }

    stop() {
        this.terminalActive = false;
    }

    handleKeyDown(event: KeyboardEvent) {
        let {keyCode, key} = event;
        if (keyCode >= F2_KEY && keyCode <= F12_KEY) {
            return;
        }

        event.preventDefault();

        const terminalId = this.store.getState().activeTerminalId;

        if (keyCode === ENTER_KEY) {
            const terminalState = this.terminalStateById(terminalId)
            if (terminalState.readOnly) return

            const submit = this.terminalSubmit[terminalId]
            if (submit) { submit() }
        } else {
            this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: terminalId});
        }
    }

    private terminalStateById(terminalId: string): TerminalState {
        const state = this.store.getState()

        switch (terminalId) {
            case MAIN_TERMINAL_ID : return state.terminal
            case ICE_INPUT_TERMINAL_ID: return state.inputTerminal
        }
        throw new Error("Unknown terminalId: " + terminalId)
    }


    registerTerminalSubmit(terminalId: string, submit: () => void) {
        this.terminalSubmit[terminalId] = submit
    }
}

export const terminalManager = new TerminalManager()
