import React from "react";
import {useSelector} from "react-redux";
import {StatusLightState} from "./StatusLightReducers";
import {getReadableTextColor} from "./contrast-color";

const MARGIN = 23

export const StatusLight = () => {
    const state: StatusLightState = useSelector((state: StatusLightState) => state)

    if (state.currentOption === null) {
        return <div className="container-fluid" data-bs-theme="dark">
            <div className="d-flex justify-content-center">
                <h1 style={{color: "#333"}}><br/>Connecting...</h1>
            </div>
        </div>
    }



    const screenWidth = document.documentElement.clientWidth
    const screenHeight = document.documentElement.clientHeight
    const size = Math.min(screenWidth, screenHeight)
    const sizeMinMargin = size - (2 * MARGIN)
    const spacerHeight = (screenHeight - size) / 2


    const text = state.options[state.currentOption]?.text || ""
    const color = state.options[state.currentOption]?.color || "grey"
    const contrastColor = getReadableTextColor(color)

    return (
        <>
            <div style={{height: spacerHeight}}/>
            <div className="container-fluid" data-bs-theme="dark">
                <div className="d-flex justify-content-center">
                    <div style={{"--status-light-color": color, height: sizeMinMargin, width: sizeMinMargin} as React.CSSProperties}
                         className="status-light-color-box status-light">
                        <div className="d-flex justify-content-center">
                            <br/>
                            <br/>
                            <h1 style={{color: contrastColor}}> {text} </h1>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

