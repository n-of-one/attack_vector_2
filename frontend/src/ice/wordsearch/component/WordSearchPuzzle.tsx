import React from 'react'
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {useSelector} from "react-redux";
import {LetterState, WordSearchState} from "../reducer/WordSearchStateReducer";
import {LetterGrid} from "./LetterGrid";


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

    return <table
        onMouseDown={(event) => mouseDown(event)}
        onMouseUp={(event) => mouseUp(event)}
        onMouseMove={(event) => mouseMove(event)}
    >
        <tbody>
            <LetterGrid />
        </tbody>
    </table>


}



