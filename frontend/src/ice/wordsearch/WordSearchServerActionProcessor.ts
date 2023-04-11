import {Store} from "redux";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {SERVER_ENTER_ICE_WORD_SEARCH, SERVER_ICE_WORD_SEARCH_UPDATED} from "./reducer/WordSearchPuzzleReducer";
import {wordSearchManager} from "./component/WordSearchManager";
import {delay} from "../../common/Util";

export interface ServerEnterIceWordSearch {
    layerId: string,
    strength: string,
    letters: string[][],
    words: string[],
    uiState: string,
    lettersCorrect: [],
    lettersSelected: [],
    wordIndex: 0,
    hacked: false,
}


export const initWordSearchServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_ENTER_ICE_WORD_SEARCH, (data: ServerEnterIceWordSearch) => {
        const iceId = store.getState().iceId
        wordSearchManager.enter(iceId, data)
    })
    webSocketConnection.addAction(SERVER_ICE_WORD_SEARCH_UPDATED, (data: ServerEnterIceWordSearch) => {
        delay(() => {
            wordSearchManager.update(data)
        })
    })

}