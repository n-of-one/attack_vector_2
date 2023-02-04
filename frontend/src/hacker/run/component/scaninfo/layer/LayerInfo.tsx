import React from 'react'
import {ICE_PASSWORD, ICE_TANGLE, OS, TEXT, TIMER_TRIGGER} from "../../../../../common/enums/LayerTypes"
import {ScanInfoOs} from "./ScanInfoOs"
import {ScanInfoText} from "./ScanInfoText"
import {Pad} from "../../../../../common/component/Pad"
import {ScanInfoIce} from "./ScanInfoIce"
import {ScanInfoTimerTrigger} from "./ScanInfoTimerTrigger"
import {LayerDetails} from "../../../../../editor/reducer/NodesReducer";

const renderLayer = (layer: LayerDetails) => {
    switch (layer.type) {
        case OS:
            return <ScanInfoOs layer={layer}/>
        case ICE_PASSWORD:
            return <ScanInfoIce layer={layer} iceDescription="static password"/>
        case ICE_TANGLE:
            return <ScanInfoIce layer={layer} iceDescription="tangle"/>
        case TEXT:
            return <ScanInfoText layer={layer}/>
        case TIMER_TRIGGER:
            return <ScanInfoTimerTrigger layer={layer}/>
        default:
            return <><span className="text-danger">Unknown layer</span></>
    }
}

export const LayerInfo = ({layer}:{layer: LayerDetails}) => {
    return (
        <>
            <Pad length={3} numberValue={layer.level}/>
            <span className="text-primary">{layer.level}</span>
            <Pad length={3}/>{layer.name}{renderLayer(layer)}<br/>
        </>
    )
}