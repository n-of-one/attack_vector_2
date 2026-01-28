import React from "react";
import {LayerDetails} from "../../../../../common/sites/SiteModel";
export const ScanInfoCore = ({layer}: {layer: LayerDetails}) => {
    const reveal = layer.revealNetwork ? "yes" : "no"
    return (
        <> - reveal network: <span className="text-info" >{reveal}</span></>
    )
}
