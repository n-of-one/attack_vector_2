import React from "react";
import {LayerDetails} from "../../../../../editor/reducer/NodesReducer";

export const ScanInfoTripwire = ({layer}: {layer: LayerDetails}) => {
    return (
        <> - reset after: <span className="text-warning" >{layer.countdown}</span> and shutdown for <span className="text-warning" >{layer.shutdown}</span></>
    )
}
