import {createTerminalReducer, TerminalLineType, TerminalState} from "../../common/terminal/TerminalReducer";
import {CHAT_TERMINAL_ID, ICE_DISPLAY_TERMINAL_ID, ICE_INPUT_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {PasswordIceI, passwordIceReducer} from "./container/PasswordIceReducer";


export interface PasswordRootState {
    iceId: string,
    password: PasswordIceI,
    displayTerminal: TerminalState,
    inputTerminal: TerminalState,
    chatTerminal: TerminalState,
    activeTerminalId: string,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});
const inputTerminalReducer = createTerminalReducer(ICE_INPUT_TERMINAL_ID, {renderOutput: false});
const chatTerminalReducer = createTerminalReducer(CHAT_TERMINAL_ID, {readOnly: true, receiveBuffer: [{type: TerminalLineType.TEXT, data: "= chat offline ="}]})

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const passwordRootReducer = combineReducers<PasswordRootState>(
    {
        iceId: noOpReducer,
        password: passwordIceReducer,
        activeTerminalId: noOpReducer,
        displayTerminal: displayTerminalReducer,
        inputTerminal: inputTerminalReducer,
        chatTerminal: chatTerminalReducer,

    }
)

