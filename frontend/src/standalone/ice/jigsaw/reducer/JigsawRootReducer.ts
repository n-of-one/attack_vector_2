import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {JigsawUiState, jigsawUiStateReducer} from "./JigsawUiStateReducer";
import {Page, pageReducer} from "../../../../common/menu/pageReducer";
import {IceHackers, iceHackersReducer} from "../../common/IceHackersReducer";

export interface JigsawRootState {
    currentPage: Page,
    hackers: IceHackers,
    ui: JigsawUiState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, autoScroll: true});

export const jigsawRootReducer = combineReducers<JigsawRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        ui: jigsawUiStateReducer,
        displayTerminal: displayTerminalReducer,
    }
)
