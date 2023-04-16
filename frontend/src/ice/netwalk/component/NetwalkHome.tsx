import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {IceTitle} from "../../../common/component/IceTitle";
import {NetwalkState} from "../reducer/NetwalkStateReducer";
import {NetwalkRootState} from "../reducer/NetwlakRootReducer";
import {netwalkCanvas} from "../canvas/NetwalkCanvas";

interface ImageLoadedProps {
    id: string,
    fileName: string
}


const imageLoaded = () => {
    netwalkCanvas.imageLoaded(12)
}

const NetwalkImage = ({id, fileName}: ImageLoadedProps) => {

    // https://codesandbox.io/s/red-flower-27i85?file=/src/gifToSprite.js

    const path = `/img/frontier/ice/netwalk/${fileName}`

    return <span><img id={id} src={path} style={{display: "none"}} height="80" width="80" onLoad={imageLoaded}/></span>
}

const DisplayTerminal = () => {
    const displayTerminal = useSelector( (rootState:NetwalkRootState) => rootState.displayTerminal )

    return <Terminal terminalState={displayTerminal} height={84} />
}


export const NetwalkHome = () => {

    const uiState = useSelector( (rootState:NetwalkRootState) => rootState.uiState )
    const state: NetwalkState = useSelector( (rootState:NetwalkRootState) => rootState.state )

    // const classShowCanvas = (uiState === HIDDEN) ? " hidden_alpha" : ""
    const classShowCanvas = ""




    return (
        <div className="row wordSearchIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Pumer" strength={state.strength} />
                    </div>
                    <div className="col-lg-9" style={{paddingTop: "4px"}}>
                        <DisplayTerminal />
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
