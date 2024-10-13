import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ActiveTerminalId, activeTerminalIdReducer, ICE_DISPLAY_TERMINAL_ID, ICE_INPUT_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers, Reducer} from "redux";
import {pageReducer} from "../../../../common/menu/pageReducer";
import {IceHackers, iceHackersReducer} from "../../common/IceHackersReducer";
import {AuthAppInfo, authInfoReducer} from "../../../app/auth/reducer/AuthInfoReducer";
import {AuthAppUi, authUiReducer} from "../../../app/auth/reducer/AuthUiReducer";
import {PasswordIceHackUi, passwordIceHackUiReducer} from "./PasswordReducer";

export interface PasswordRootState {
    currentPage: string,
    hackers: IceHackers,
    iceInfo: AuthAppInfo
    ui: AuthAppUi,
    hackUi: PasswordIceHackUi,
    displayTerminal: TerminalState,
    inputTerminal: TerminalState,
    activeTerminalId: ActiveTerminalId,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, autoScroll: true});
const inputTerminalReducer = createTerminalReducer(ICE_INPUT_TERMINAL_ID, {renderOutput: false});

export const passwordRootReducer: Reducer<PasswordRootState> = combineReducers<PasswordRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        iceInfo: authInfoReducer,
        ui: authUiReducer,
        hackUi: passwordIceHackUiReducer,
        activeTerminalId: activeTerminalIdReducer,
        displayTerminal: displayTerminalReducer,
        inputTerminal: inputTerminalReducer,
    }
)
