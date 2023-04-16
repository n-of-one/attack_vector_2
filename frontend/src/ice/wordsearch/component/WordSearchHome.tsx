import React, {createRef} from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {WordSearchPuzzle} from "./WordSearchPuzzle";
import {IceTitle} from "../../../common/component/IceTitle";
import {HIDDEN} from "../../IceModel";

export const WordSearchHome = () => {

    const puzzle = useSelector( (state:WordSearchRootState) => state.puzzle )
    const displayTerminal = useSelector( (state:WordSearchRootState) => state.displayTerminal )
    const uiState = useSelector( (state:WordSearchRootState) => state.uiState )

    const classShowCanvas = (uiState === HIDDEN) ? " hidden_alpha" : ""

    const puzzleRef = createRef<HTMLDivElement>()

    const moveIt = () => {
        // alert('moving it')
        const canvasContainerRef = document.getElementsByClassName("canvas-container")?.item(0)
        if (!canvasContainerRef || !puzzleRef.current) {
            setTimeout(moveIt, 2)
            return
        }
        canvasContainerRef.appendChild(puzzleRef.current)
    }
    setTimeout(moveIt, 2)

    return (
        <div className="row wordSearchIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Pumer" strength={puzzle.strength} />
                    </div>
                    <div className="col-lg-9" style={{paddingTop: "4px"}}>
                        <Terminal terminalState={displayTerminal} height={84} />
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
