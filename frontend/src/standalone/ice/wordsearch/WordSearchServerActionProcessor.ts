import {Store} from "redux";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {SERVER_WORD_SEARCH_ENTER} from "./reducer/WordSearchPuzzleReducer";
import {wordSearchManager} from "./WordSearchManager";
import {delay} from "../../../common/util/Util";
import {IceStrength} from "../../../common/model/IceStrength";

export const SERVER_WORD_SEARCH_UPDATED = "SERVER_WORD_SEARCH_UPDATED"

export interface ServerEnterIceWordSearch {
    layerId: string,
    strength: IceStrength,
    letterGrid: string[][],
    words: string[],
    uiState: string,
    correctPositions: [],
    solutions: string[][]
    wordIndex: 0,
    hacked: false,
}


export interface UpdateAction {
    iceId: string,
    lettersCorrect: string[],
    wordIndex: number,
    hacked: boolean
}

export const initWordSearchServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_WORD_SEARCH_ENTER, (data: ServerEnterIceWordSearch) => {
        const iceId = store.getState().iceId
        wordSearchManager.enter(iceId, data)
    })
    webSocketConnection.addAction(SERVER_WORD_SEARCH_UPDATED, (data: UpdateAction) => {
        delay(() => {
            wordSearchManager.update(data)
        })
    })
}