import React from 'react'
import {NETWALK_ICE, OS, PASSWORD_ICE, TAR_ICE, TANGLE_ICE, TEXT, TIMER_TRIGGER, WORD_SEARCH_ICE} from "../../../../../common/enums/LayerTypes"
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
        case TEXT:
            return <ScanInfoText layer={layer}/>
        case TIMER_TRIGGER:
            return <ScanInfoTimerTrigger layer={layer}/>
        case PASSWORD_ICE:
            return <ScanInfoIce layer={layer} iceDescription="static password"/>
        case TANGLE_ICE:
            return <ScanInfoIce layer={layer} iceDescription="tangle"/>
        case WORD_SEARCH_ICE:
            return <ScanInfoIce layer={layer} iceDescription="word search"/>
        case NETWALK_ICE:
            return <ScanInfoIce layer={layer} iceDescription="netwalk"/>
        case TAR_ICE:
            return <ScanInfoIce layer={layer} iceDescription="tar"/>
        default:
            return <><span className="text-danger"> Unknown layer</span></>
    }
}

export const LayerInfo = ({layer, revealed}:{layer: LayerDetails, revealed: boolean} ) => {

    const layerDetails = revealed ?
        <><Pad length={3}/>{layer.name}{renderLayer(layer)}<br/></> :
        <><Pad length={3}/>unknown (shielded by ICE)<br/></>

    return (
        <>
            <Pad length={3} numberValue={layer.level}/>
            <span className="text-primary">{layer.level}</span>
            {layerDetails}
        </>
    )
}
