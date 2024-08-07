import React, {createRef} from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {WordSearchPuzzle} from "./WordSearchPuzzle";
import {IceTitle} from "../../common/IceTitle";
import {HIDDEN} from "../../common/IceModel";
import {CloseTabButton} from "../../common/CloseTabButton";

export const WordSearchHome = () => {

    const puzzle = useSelector( (state:WordSearchRootState) => state.puzzle )
    const displayTerminal = useSelector( (state:WordSearchRootState) => state.displayTerminal )
    const uiState = useSelector( (state:WordSearchRootState) => state.uiState )

    const classShowCanvas = (uiState === HIDDEN) ? " hidden_alpha" : ""

    const puzzleRef = createRef<HTMLDivElement>()

    const moveIt = () => {
        const canvasContainerRef = document.getElementsByClassName("canvas-container")?.item(0)
        if (!canvasContainerRef || !puzzleRef.current) {
            setTimeout(moveIt, 2)
            return
        }
        canvasContainerRef.appendChild(puzzleRef.current)
    }
    setTimeout(moveIt, 2)

    return (
        <div className="row icePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Jaal" strength={puzzle.strength}/>
                    </div>
                    <div className="col-lg-8" style={{paddingTop: "4px"}}>
                        <Terminal terminalState={displayTerminal} height={84}/>
                    </div>
                    <div className="col-lg-1">
                        <div className="float-end">
                            <CloseTabButton/>
                        </div>
                    </div>
                </div>

                <div className={"row transition_alpha_fast" + classShowCanvas}>
                    <div className="col-lg-12">
                        <div>
                            <canvas id="wordSearchCanvas" style={{
                                "borderRadius": "3px 3px 3px 3px",
                                "marginTop": "10px",
                                "marginBottom": "10px",
                            }}/>
                        </div>
                    </div>
                    <div id="letterGrid" className="iceWsGrid transition_alpha_fast" ref={puzzleRef}>
                        <WordSearchPuzzle />
                    </div>
                </div>

            </div>
        </div>
    )
}
