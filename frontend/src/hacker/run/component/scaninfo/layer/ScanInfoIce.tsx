import React from "react"
import {Pad} from "../../../../../common/component/Pad"
import {LayerDetails} from "../../../../../common/sites/SiteModel";
export const ScanInfoIce =({layer, iceDescription, showStrength}: {layer: LayerDetails, iceDescription: string, showStrength: boolean}) => {

    const hackedText = layer.hacked ? "Yes" : "No"

    const strengthIndication = showStrength ? <><Pad length={8} />Strength: <span className="text-info">{layer.strength}</span><br/></> : <></>

    return (
        <>
            &nbsp;ICE ({iceDescription})<br/>
            {strengthIndication}
            <Pad length={8} />Hacked: <span className="text-danger">{hackedText}</span>
        </>
    )
}

