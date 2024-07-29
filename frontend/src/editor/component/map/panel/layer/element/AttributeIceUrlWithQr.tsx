import React from "react";
import {AttributeUrlWithQr} from "./AttributeUrlWithQr";

interface Props {
    layerId: string
}

export const AttributeIceUrlWithQr = ({layerId}: Props) => {

    return <AttributeUrlWithQr name="Hacker URL" type="ice" layerId={layerId} description="Hacker link to ICE"/>

}
