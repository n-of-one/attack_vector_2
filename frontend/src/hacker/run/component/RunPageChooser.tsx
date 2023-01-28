import React from 'react';
import {useSelector} from "react-redux";
import {RunHome} from "./RunHome";
import {IceGame} from "../ice/IceGame";
import {CountdownTimer} from "../coundown/CountdownTimer";
import {HackerState} from "../../HackerRootReducer";
import {CurrentIce} from "../ice/CurrentIceReducer";

const ice = (currentIce: CurrentIce) => {
    if (currentIce.type) {
        return <IceGame/>
    } else {
        return <></>
    }
}

export const RunPageChooser = () => {
    const currentIce = useSelector((state: HackerState) => state.run.ice.currentIce)
    const runHomeStyle = (currentIce.type) ? "none" : "inline";

    return <>
        <CountdownTimer/>
        {ice(currentIce)}
        <span style={{"display": runHomeStyle}}>
                    <RunHome/>
                </span>
    </>
}
