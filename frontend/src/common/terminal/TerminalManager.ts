import {ENTER_KEY, F12_KEY, F2_KEY} from "../util/KeyCodes";
import {delay} from "../util/Util";
import {Dispatch, Store} from "redux";
import {TERMINAL_CLEAR, TERMINAL_KEY_PRESS, TerminalState, TICK} from "./TerminalReducer";
import {ICE_INPUT_TERMINAL_ID, MAIN_TERMINAL_ID} from "./ActiveTerminalIdReducer";

export const TERMINAL_UPDATE_MILLIS = 20 // 50 updates/s

class TerminalManager {

    store: Store = null as unknown as Store
    dispatch = null as unknown as Dispatch
    terminalActive: boolean = false

    terminalSubmit: { [key: string]: () => void } = {}

    async init(store: Store) {
        this.store = store;
        this.dispatch = store.dispatch;
        delay(() => {
            let currentSecond = Math.floor(Date.now() / 1000)

            setInterval(() => {
                const tickSecond = Math.floor(Date.now() / 1000)
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

        // Use the browser's paste event instead of navigator.clipboard.readText(): Firefox and Safari show a "Paste" confirmation button for readText()
        document.onpaste =(event: ClipboardEvent) => {
            if (this.terminalActive) {
                this.handlePaste(event)
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
        if (key === "Control" || key === "Alt" || key === "Shift" || key === "Meta" || (keyCode >= F2_KEY && keyCode <= F12_KEY)) {
            return // Ignore CTLR, ALT, SHIFT, META (Cmd on Mac), F2-F12
        }
        const lowerKey = key.toLowerCase()
        const ctlrOrMeta = event.ctrlKey || event.metaKey
        if (ctlrOrMeta && lowerKey === "c") return // Copy
        if (ctlrOrMeta && lowerKey === "v") return // Paste

        const activeElement = document.activeElement
        if (activeElement instanceof HTMLInputElement) {
            return
        } // Ignore if an input field is active

        event.preventDefault();

        const terminalId = this.store.getState().activeTerminalId;
        if (ctlrOrMeta && lowerKey === "l") {
            this.dispatch({type: TERMINAL_CLEAR, terminalId: terminalId})
            return
        }

        if (keyCode === ENTER_KEY) {
            const terminalState = this.terminalStateById(terminalId)
            if (terminalState.readOnly) return

            const submit = this.terminalSubmit[terminalId]
            if (submit) {
                submit()
            }
        } else {
            this.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: terminalId, pastedText: null})
        }
    }

    handlePaste(event: ClipboardEvent) {
        if (document.activeElement instanceof HTMLInputElement) {
            return
        } // Ignore if an input field is active

        const pastedText = event.clipboardData?.getData("text/plain")
        if (!pastedText) return
        event.preventDefault()

        const terminalId = this.store.getState().activeTerminalId
        this.dispatch({type: TERMINAL_KEY_PRESS, key: "", keyCode: 0, terminalId: terminalId, pastedText: pastedText})
    }

    private terminalStateById(terminalId: string): TerminalState {
        const state = this.store.getState()

        switch (terminalId) {
            case MAIN_TERMINAL_ID :
                return state.terminal
            case ICE_INPUT_TERMINAL_ID:
                return state.inputTerminal
        }
        throw new Error("Unknown terminalId: " + terminalId)
    }


    registerTerminalSubmit(terminalId: string, submit: () => void) {
        this.terminalSubmit[terminalId] = submit
    }
}

export const terminalManager = new TerminalManager()
