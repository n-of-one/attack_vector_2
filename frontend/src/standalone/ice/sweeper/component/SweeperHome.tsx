import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {IceTitle} from "../../common/IceTitle";
import {SweeperRootState} from "../reducer/SweeperRootReducer";
import {HIDDEN} from "../../common/IceModel";
import {IceStrength} from "../../../../common/model/IceStrength";
import {sweeperCanvas} from "../canvas/SweeperCanvas";
import {SWEEPER_IMAGES} from "../canvas/SweeperCellDisplay";

/* eslint jsx-a11y/alt-text: 0*/

interface ImageLoadedProps {
    id: string,
    fileName: string
}

const imageLoaded = () => {
    sweeperCanvas.imageLoaded(Object.keys(SWEEPER_IMAGES).length)
}

const SweeperImage = ({id, fileName}: ImageLoadedProps) => {

    // https://codesandbox.io/s/red-flower-27i85?file=/src/gifToSprite.js

    const path = `/img/frontier/ice/sweeper/${fileName}`

    return <span><img id={id} src={path} style={{display: "none"}} onLoad={imageLoaded}/></span>
}

const DisplayTerminal = () => {
    const displayTerminal = useSelector((rootState: SweeperRootState) => rootState.displayTerminal)

    return <Terminal terminalState={displayTerminal} height={84}/>
}

export const SweeperHome = () => {

    const uiMode = useSelector((rootState: SweeperRootState) => rootState.ui.mode)
    const strength: IceStrength = useSelector((rootState: SweeperRootState) => rootState.ui.strength)

    const classShowCanvas = (uiMode === HIDDEN) ? " hidden_alpha" : ""


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
                        <IceTitle name="SWEEPER" strength={strength}/>
                    </div>
                    <div className="col-lg-9" style={{paddingTop: "4px"}}>
                        <DisplayTerminal/>
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
            <SweeperImages/>
        </div>
    )
}

const SweeperImages = () => {
    return <>
        {Object.values(SWEEPER_IMAGES).map((image) => {
            return <SweeperImage id={image.id} fileName={image.fileName} key={image.id}/>
        })
        }</>
}