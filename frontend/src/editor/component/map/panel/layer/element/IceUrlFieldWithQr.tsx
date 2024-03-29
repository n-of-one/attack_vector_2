import React from "react";
import {UrlFieldWithQr} from "./UrlFieldWithQr";

interface Props {
    layerId: string
}

export const IceUrlFieldWithQr = ({layerId}: Props) => {

    return <UrlFieldWithQr name="Hacker URL" type="ice" layerId={layerId} description="Hacker link to ICE"/>

}
