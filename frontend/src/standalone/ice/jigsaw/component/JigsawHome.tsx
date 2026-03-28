import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {IceTitle} from "../../common/IceTitle";
import {JigsawRootState} from "../reducer/JigsawRootReducer";
import {HIDDEN} from "../../common/IceModel";
import {IceStrength} from "../../../../common/model/IceStrength";
import {jigsawCanvas} from "../canvas/JigsawCanvas";
import {CloseTabButton} from "../../common/CloseTabButton";

/* eslint jsx-a11y/alt-text: 0*/

export const JigsawHome = () => {

    const uiMode = useSelector((rootState: JigsawRootState) => rootState.ui.mode)
    const strength: IceStrength = useSelector((rootState: JigsawRootState) => rootState.ui.strength)

    const classShowCanvas = (uiMode === HIDDEN) ? " hidden_alpha" : ""

    return (
        <div className="row icePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Paheli" strength={strength}/>
                    </div>
                    <div className="col-lg-7" style={{paddingTop: "4px"}}>
                        <DisplayTerminal/>
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
                            <canvas id="jigsawCanvas" style={{
                                "borderRadius": "3px 3px 3px 3px",
                                "marginTop": "10px",
                                "marginBottom": "10px",
                            }}/>
                        </div>
                    </div>
                </div>
            </div>
            <JigsawSourceImage/>
        </div>
    )
}


const JigsawSourceImage = () => {
    const path = "/img/frontier/ice/jigsaw/tylijura-ai-generated-9396797_1920.png"
    return <span><img id="jigsawSourceImage" src={path} style={{display: "none"}} onLoad={() => {
        jigsawCanvas.imageLoaded()
    }}/></span>
}

const DisplayTerminal = () => {
    const displayTerminal = useSelector((rootState: JigsawRootState) => rootState.displayTerminal)
    return <Terminal terminalState={displayTerminal} height={84}/>
}
