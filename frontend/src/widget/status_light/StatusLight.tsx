import React, {useEffect} from "react";
import {useSelector} from "react-redux";
import {statusLightReducer, StatusLightState} from "./StatusLightReducers";

const MARGIN = 23

export const StatusLight = () => {

    const state: StatusLightState = useSelector((state: StatusLightState) => state)

    const statusClassName = statusClass(state.status)
    const className = `status-light ${statusClassName}`

    const screenWidth = document.documentElement.clientWidth
    const screenHeight = document.documentElement.clientHeight
    const size = Math.min(screenWidth, screenHeight)
    const sizeMinMargin = size - (2 * MARGIN)

    const spacerHeight = (screenHeight - size) / 2

    let text = "Connecting..."
    if (state.status !== null) {
        text = state.status ?  state.textForGreen : state.textForRed
    }
    const textClassName = (state.status) ? "status-light-text-green" : "status-light-text-red"

    return (
        <>
        <div style={{height: spacerHeight}} />
        <div className="container-fluid" data-bs-theme="dark">
            <div className="d-flex justify-content-center">
                <div className={className} style={{height: sizeMinMargin, width: sizeMinMargin}}>
                    <div className="d-flex justify-content-center">
                        <br />
                        <br />
                    <h1 className={textClassName}>
                    {text}
                        </h1>
                    </div>
                </div>
            </div>
        </div>
        </>
    )
}

const statusClass = (status: boolean | null) => {
    if (status === null) {
        return "status-light-unknown"
    }
    if (status) {
        return "status-light-on"
    }
    return "status-light-off"

}