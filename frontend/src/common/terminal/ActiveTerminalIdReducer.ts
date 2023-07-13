import {AnyAction} from "redux";

export const CHAT_TERMINAL_ID = "chat"
export const MAIN_TERMINAL_ID = "main"
export const ICE_INPUT_TERMINAL_ID = "iceInput"
export const ICE_DISPLAY_TERMINAL_ID = "iceDisplay"

export type ActiveTerminalId = "main" |  "iceInput"

const defaultState: ActiveTerminalId = MAIN_TERMINAL_ID;

export const activeTerminalIdReducer = (state: ActiveTerminalId = defaultState, action: AnyAction): ActiveTerminalId => {
    switch (action.type) {
        default:
            return state;
    }
}
