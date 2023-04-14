import React from 'react'
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {useSelector} from "react-redux";
import {LetterState, WordSearchState} from "../reducer/WordSearchStateReducer";


const mouseDown = (event: React.MouseEvent<HTMLTableElement>) => {
    wordSearchCanvas.mouseDown(event.nativeEvent)
}

const mouseUp = (event: React.MouseEvent<HTMLTableElement>) => {
    wordSearchCanvas.mouseUp(event.nativeEvent)
}

const mouseMove = (event: React.MouseEvent<HTMLTableElement>) => {
    wordSearchCanvas.mouseMove(event.nativeEvent)
}

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

    if (state.selected[key] !== undefined) { return "wsSELECTED" }

    const letterState: LetterState | undefined  = state.letters[key]

    switch(letterState) {
        case undefined: return ""
        case LetterState.HINT : return "wsSOLUTION"
        case LetterState.CORRECT: return "wsCORRECT"
        case LetterState.CORRECT_HIGHLIGHT: return "wsCORRECT_HIGHLIGHT"
        default: return "wsUNKNOWN"
    }
}

export const WordSearchPuzzle = () => {

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

        return <tr className="iceWsRow" key={y}>{
            row.map(puzzleElementForRow)
        }</tr>
    }


    return <table
        onMouseDown={(event) => mouseDown(event)}
        onMouseUp={(event) => mouseUp(event)}
        onMouseMove={(event) => mouseMove(event)}
    >
        <tbody>
        {letterGrid.map(puzzleRow)}
        </tbody>
    </table>


}



