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


export const WordSearchPuzzle = () => {

    const puzzle = useSelector( (rootState:WordSearchRootState) => rootState.puzzle )
    const state = useSelector( (rootState:WordSearchRootState) => rootState.state )



    const puzzleRow = (row: string[], rowIndex: number) => {

        const puzzleElementForRow = (element: string, index: number) => {
            const key = `${rowIndex}:${index}`


            const classCorrect = (state.lettersCorrect.includes(key) ) ? `wsCORRECT` : ""
            const classSelected = (state.lettersSelected.includes(key) ) ? `wsSELECTED` : classCorrect

            return <td className="iceWsContainer" key={key}>
                <div className={`iceWsElement ${classSelected}`}>
                    <div className={`ws ws${element}`} />
                </div>
            </td>
        }

        return <tr className="iceWsRow" key={`row:${rowIndex}`}>{
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



