import React from 'react'
import {LayerType} from "../../../../common/enums/LayerTypes"
import {Pad} from "../../../../common/component/Pad"
import {ScanInfoIce} from "./layer/ScanInfoIce"
import {ScanInfoTripwire} from "./layer/ScanInfoTripwire";
import {ScanInfoCore} from "./layer/ScanInfoCore";
import {LayerDetails} from "../../../../common/sites/SiteModel";

const renderLayer = (layer: LayerDetails) => {
    switch (layer.type) {
        case LayerType.OS:
        case LayerType.TEXT:
        case LayerType.KEYSTORE:
        case LayerType.LOCK:
        case LayerType.STATUS_LIGHT:
        case LayerType.SCRIPT_INTERACTION:
        case LayerType.SCRIPT_CREDITS:
            return <></>
        case LayerType.TRIPWIRE:
            return <ScanInfoTripwire layer={layer}/>
        case LayerType.CORE:
            return <ScanInfoCore layer={layer}/>
        case LayerType.PASSWORD_ICE:
            return <ScanInfoIce layer={layer} iceDescription="static password"/>
        case LayerType.TANGLE_ICE:
            return <ScanInfoIce layer={layer} iceDescription="tangle"/>
        case LayerType.WORD_SEARCH_ICE:
            return <ScanInfoIce layer={layer} iceDescription="word search"/>
        case LayerType.NETWALK_ICE:
            return <ScanInfoIce layer={layer} iceDescription="netwalk"/>
        case LayerType.TAR_ICE:
            return <ScanInfoIce layer={layer} iceDescription="tar"/>
        case LayerType.SWEEPER_ICE:
            return <ScanInfoIce layer={layer} iceDescription="minesweeper"/>
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
