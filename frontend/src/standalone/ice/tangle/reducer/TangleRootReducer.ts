import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {tangleIceReducer, TangleIceState} from "./TangleIceReducer";
import {Page, pageReducer} from "../../../../common/menu/pageReducer";
import {IceHackers, iceHackersReducer} from "../../common/IceHackersReducer";


export interface TangleRootState {
    currentPage: Page,
    hackers: IceHackers,
    tangle: TangleIceState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, autoScroll: true});

export const tangleRootReducer = combineReducers<TangleRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        tangle: tangleIceReducer,
        displayTerminal: displayTerminalReducer,
    }
)

