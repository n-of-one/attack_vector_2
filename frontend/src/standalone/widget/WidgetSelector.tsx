import React from "react";
import {TopLevelError} from "../../common/component/TopLevelError";
import {StatusLightRoot} from "./statusLight/StatusLightRoot";


interface Props {
    type: string,
    layerId: string,
}

export const WidgetSelector = ({type, layerId}: Props) => {

    switch(type) {
        case "statusLight":
            return <StatusLightRoot layerId={layerId}/>
        default:
            return <TopLevelError error="Invalid connection"
                                  description={`(Unknown widget type: ${type}, maybe the QR code/URL is from an older/different version of Attack Vector?)`}/>
    }
}
