import React from "react"
import {Pad} from "../../../../../common/component/Pad"
import {LayerDetails} from "../../../../../common/sites/SiteModel";
export const ScanInfoIce =({layer, iceDescription}: {layer: LayerDetails, iceDescription: string}) => {

    const hackedText = layer.hacked ? "Yes" : "No"
    return (
        <>
            &nbsp;ICE ({iceDescription})<br/>
            <Pad length={8} />Strength: <span className="text-info">{layer.strength}</span><br/>
            <Pad length={8} />Hacked: <span className="text-danger">{hackedText}</span>
        </>
    )
}

