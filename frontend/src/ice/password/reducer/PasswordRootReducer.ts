import {createTerminalReducer, TerminalLineType, TerminalState} from "../../../common/terminal/TerminalReducer";
import {
    ActiveTerminalId,
    activeTerminalIdReducer,
    CHAT_TERMINAL_ID,
    ICE_DISPLAY_TERMINAL_ID,
    ICE_INPUT_TERMINAL_ID
} from "../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers, Reducer} from "redux";
import {pageReducer} from "../../../common/menu/pageReducer";
import {iceHackersReducer, IceHackers} from "../../common/IceHackersReducer";
import {AuthAppInfo, authAppInfoReducer} from "../../../app/authApp/reducer/AuthAppInfoReducer";
import {AuthAppUi, authAppUiReducer} from "../../../app/authApp/reducer/AuthAppUiReducer";
import {PasswordIceHackUi, passwordIceHackUiReducer} from "./PasswordReducer";

export interface PasswordRootState {
    currentPage: string,
    hackers: IceHackers,
    iceInfo: AuthAppInfo
    ui: AuthAppUi,
    hackUi: PasswordIceHackUi,
    displayTerminal: TerminalState,
    inputTerminal: TerminalState,
    chatTerminal: TerminalState,
    activeTerminalId: ActiveTerminalId,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});
const inputTerminalReducer = createTerminalReducer(ICE_INPUT_TERMINAL_ID, {renderOutput: false});
const chatTerminalReducer = createTerminalReducer(CHAT_TERMINAL_ID, {readOnly: true, receiveBuffer: [{type: TerminalLineType.TEXT, data: "= chat offline ="}]})

export const passwordRootReducer: Reducer<PasswordRootState> = combineReducers<PasswordRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        iceInfo: authAppInfoReducer,
        ui: authAppUiReducer,
        hackUi: passwordIceHackUiReducer,
        activeTerminalId: activeTerminalIdReducer,
        displayTerminal: displayTerminalReducer,
        inputTerminal: inputTerminalReducer,
        chatTerminal: chatTerminalReducer,
    }
)
