import React from 'react'
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {useSelector} from "react-redux";
import {LetterState, WordSearchState} from "../reducer/WordSearchStateReducer";

const wsClass = (element: string): string => {
    if ((element >= 'A' && element <= 'Z')
        || (element >= 'a' && element <= 'z')) {
        return `ws${element}`
    }
    if (element >= '0' && element <= '9') {
        return `ws${element.codePointAt(0)!! - 48}`
    }

    return `ws${element.codePointAt(0)}`
}

const classByState = (key: string, state: WordSearchState) => {

    if (state.selected[key] !== undefined) {
        return "wsSELECTED"
    }

    const letterState: LetterState | undefined = state.letters[key]

    switch (letterState) {
        case undefined:
            return ""
        case LetterState.HINT :
            return "wsSOLUTION"
        case LetterState.CORRECT:
            return "wsCORRECT"
        case LetterState.CORRECT_HIGHLIGHT:
            return "wsCORRECT_HIGHLIGHT"
        default:
            return "wsUNKNOWN"
    }
}

export const LetterGrid = () => {

    const letterGrid = useSelector((rootState: WordSearchRootState) => rootState.puzzle.letterGrid)
    const state = useSelector((rootState: WordSearchRootState) => rootState.state)

    const puzzleRow = (row: string[], y: number) => {

        const puzzleElementForRow = (element: string, x: number) => {
            const key = `${x}:${y}`
            const stateClass = classByState(key, state)

            return <td className="iceWsContainer" key={key}>
                <div className={`iceWsElement ${stateClass}`}>
                    <div className={`ws ${wsClass(element)}`}/>
                </div>
            </td>
        }

        const rowKey = `${y}`
        return <tr className="iceWsRow" key={rowKey}>{
            row.map(puzzleElementForRow)
        }</tr>
    }

    return <>{letterGrid.map(puzzleRow)}</>
}



