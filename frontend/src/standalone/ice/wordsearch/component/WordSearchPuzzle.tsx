import React from 'react'
import {wordSearchCanvas} from "../canvas/WordSearchCanvas";
import {LetterGrid} from "./LetterGrid";
import {useSelector} from "react-redux";
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";


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
    const options = useSelector((state: WordSearchRootState) => state.displayOptions)

    return <table className={`wsColor_${options.color} wsFont_${options.fontSize}`}
        onMouseDown={(event) => mouseDown(event)}
        onMouseUp={(event) => mouseUp(event)}
        onMouseMove={(event) => mouseMove(event)}
    >
        <tbody>
            <LetterGrid />
        </tbody>
    </table>


}



