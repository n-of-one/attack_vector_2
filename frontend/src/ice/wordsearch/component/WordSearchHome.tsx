import React, {createRef} from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {WordSearchRootState} from "../reducer/WordSearchRootReducer";
import {WordSearchPuzzle} from "./WordSearchPuzzle";

export const WordSearchHome = () => {

    const puzzle = useSelector( (state:WordSearchRootState) => state.puzzle )
    const displayTerminal = useSelector( (state:WordSearchRootState) => state.displayTerminal )

    const classHidden = ""

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

    setTimeout( () => {
        console.log(puzzleRef!!.current!!.className)
        puzzleRef!!.current!!.classList.remove("hidden_alpha");
        console.log(puzzleRef!!.current!!.className)
    },100)

    return (
        <div className="row untangleIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-12">
                        <h4 className="text-success">
                            <strong>
                                Ice: <span className="text-info">Pumer</span>&nbsp;<br/>
                                Strength: <span className="text-info">{puzzle.strength}</span><br/>
                            </strong>
                        </h4>
                    </div>
                </div>
                <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>
                <div className="row">
                    <div className="col-lg-3">
                        <div className="text-left">
                            <div className="text">
                                Community &nbsp;avg 14:33 &nbsp;best 03:33 &nbsp;(44%)<br/>
                                You &nbsp; &nbsp; &nbsp; &nbsp;avg 12:00 &nbsp;best 08:23 &nbsp;(85%)<br/>
                                You &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; -02:33 &nbsp; &nbsp; &nbsp;+04:50<br/>
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-9">
                        <Terminal terminalState={displayTerminal} height={112}/>
                    </div>
                </div>
                <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>

                <div className={"row transition_alpha_fast" + classHidden}>
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
