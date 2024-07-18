import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {SweeperUiState, sweeperUiStateReducer} from "./SweeperUiStateReducer";
import {pageReducer} from "../../../../common/menu/pageReducer";
import {iceHackersReducer, IceHackers} from "../../common/IceHackersReducer";


export interface SweeperRootState {
    currentPage: string,
    hackers: IceHackers,
    ui: SweeperUiState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, autoScroll: true});

export const sweeperRootReducer = combineReducers<SweeperRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        ui: sweeperUiStateReducer,
        displayTerminal: displayTerminalReducer,
    }
)

