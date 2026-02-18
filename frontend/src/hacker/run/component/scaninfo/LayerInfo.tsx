import React from 'react'
import {LayerType} from "../../../../common/enums/LayerTypes"
import {Pad} from "../../../../common/component/Pad"
import {ScanInfoIce} from "./layer/ScanInfoIce"
import {ScanInfoShutdownAccelerator, ScanInfoTripwire} from "./layer/ScanInfoTripwire";
import {ScanInfoCore} from "./layer/ScanInfoCore";
import {LayerDetails} from "../../../../common/sites/SiteModel";
import {assertNever} from "../../../../common/util/Assert";

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
        case LayerType.TIMER_ADJUSTER:
            return <ScanInfoShutdownAccelerator layer={layer}/>
        case LayerType.CORE:
            return <ScanInfoCore layer={layer}/>
        case LayerType.PASSWORD_ICE:
            return <ScanInfoIce layer={layer} iceDescription="static password" showStrength={false}/>
        case LayerType.TANGLE_ICE:
            return <ScanInfoIce layer={layer} iceDescription="tangle" showStrength={true}/>
        case LayerType.WORD_SEARCH_ICE:
            return <ScanInfoIce layer={layer} iceDescription="word search" showStrength={true}/>
        case LayerType.NETWALK_ICE:
            return <ScanInfoIce layer={layer} iceDescription="netwalk" showStrength={true}/>
        case LayerType.TAR_ICE:
            return <ScanInfoIce layer={layer} iceDescription="tar" showStrength={true}/>
        case LayerType.SWEEPER_ICE:
            return <ScanInfoIce layer={layer} iceDescription="minesweeper" showStrength={true}/>
        default:
            assertNever(layer.type)
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
