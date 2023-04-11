import React from 'react'
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {useSelector} from "react-redux";


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

export const WordSearchPuzzle = () => {

    const puzzle = useSelector((rootState: WordSearchRootState) => rootState.puzzle)
    const state = useSelector((rootState: WordSearchRootState) => rootState.state)


    const puzzleRow = (row: string[], y: number) => {

        const puzzleElementForRow = (element: string, x: number) => {
            const key = `${x}:${y}`


            const classCorrect = (state.lettersCorrect.includes(key)) ? `wsCORRECT` : ""
            const classSelected = (state.lettersSelected.includes(key)) ? `wsSELECTED` : classCorrect


            return <td className="iceWsContainer" key={key}>
                <div className={`iceWsElement ${classSelected}`}>
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
        {puzzle.letters.map(puzzleRow)}
        </tbody>
    </table>


}



