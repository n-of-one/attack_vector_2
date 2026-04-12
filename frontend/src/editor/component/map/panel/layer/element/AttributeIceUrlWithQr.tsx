import React from "react";
import {AttributeUrlWithQr} from "./AttributeUrlWithQr";

interface Props {
    layerId: string,
    fileName: string,
}

export const AttributeIceUrlWithQr = ({layerId, fileName}: Props) => {

    return <AttributeUrlWithQr name="Standalone URL" type="ice" subType="externalAccess" layerId={layerId}
                               description="Link to ICE that can be accessed without hacking into the site."
                               fileName={fileName}/>

}
