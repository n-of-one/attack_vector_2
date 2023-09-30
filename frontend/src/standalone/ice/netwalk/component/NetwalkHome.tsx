import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {IceTitle} from "../../common/IceTitle";
import {NetwalkRootState} from "../reducer/NetwalkRootReducer";
import {netwalkCanvas} from "../canvas/NetwalkCanvas";
import {HIDDEN} from "../../IceModel";
import {IceStrength} from "../../../../common/model/IceStrength";

/* eslint jsx-a11y/alt-text: 0*/

interface ImageLoadedProps {
    id: string,
    fileName: string
}

const imageLoaded = () => {
    netwalkCanvas.imageLoaded(12)
}

const NetwalkImage = ({id, fileName}: ImageLoadedProps) => {

    // https://codesandbox.io/s/red-flower-27i85?file=/src/gifToSprite.js

    const path = `/img/frontier/ice/netwalk/cell/${fileName}`

    return <span><img id={id} src={path} style={{display: "none"}} height="80" width="80" onLoad={imageLoaded}/></span>
}

const DisplayTerminal = () => {
    const displayTerminal = useSelector( (rootState:NetwalkRootState) => rootState.displayTerminal )

    return <Terminal terminalState={displayTerminal} height={84} />
}


export const NetwalkHome = () => {

    const uiState = useSelector( (rootState:NetwalkRootState) => rootState.state.uiState )
    const strength: IceStrength = useSelector( (rootState:NetwalkRootState) => rootState.state.strength )

    const classShowCanvas = (uiState === HIDDEN) ? " hidden_alpha" : ""


    // Code used to download the canvas in order to create the background image
    // setTimeout(() => {
    //     const a = document.getElementById("downloadSpan")!!
    //     let link = document.createElement("a")
    //     link.download = "netwalk.png"
    //     link.href = netwalkCanvas.canvas.toDataURL({
    //         format: 'jpg',
    //     });
    //     link.text = "Download to png"
    //     a.appendChild(link)
    // }, 1000)



    return (
        <div className="row icePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Sanrachana" strength={strength} />
                    </div>
                    <div className="col-lg-9" style={{paddingTop: "4px"}}>
                        <DisplayTerminal />
                        {/*<span id={"downloadSpan"}/>*/}
                    </div>
                </div>

                <div className={"row transition_alpha_fast" + classShowCanvas}>
                    <div className="col-lg-12">
                        <div>
                            <canvas id="netwalkCanvas" style={{
                                "borderRadius": "3px 3px 3px 3px",
                                "marginTop": "10px",
                                "marginBottom": "10px",
                            }}/>
                        </div>
                    </div>
                </div>
            </div>
            <NetwalkImage id="center" fileName="center.png" />
            <NetwalkImage id="centerConnected" fileName="centerConnected.gif" />
            <NetwalkImage id="corner" fileName="corner.png" />
            <NetwalkImage id="cornerConnected" fileName="cornerConnected.gif" />
            <NetwalkImage id="cross" fileName="cross.png" />
            <NetwalkImage id="crossConnected" fileName="crossConnected.gif" />
            <NetwalkImage id="end" fileName="end.png" />
            <NetwalkImage id="endConnected" fileName="endConnected.gif" />
            <NetwalkImage id="split" fileName="split.png" />
            <NetwalkImage id="splitConnected" fileName="splitConnected.gif" />
            <NetwalkImage id="straight" fileName="straight.png" />
            <NetwalkImage id="straightConnected" fileName="straightConnected.gif"/>
        </div>
    )
}
