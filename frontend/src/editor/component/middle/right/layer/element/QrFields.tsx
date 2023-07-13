import React from "react";
import {UrlFieldWithQr} from "./UrlFieldWithQr";

interface Props {
    id: string
}

export const QrFields = ({  id } : Props) => {

    return <>
        <UrlFieldWithQr name="Hacker URL" type="ice" id={id} description="Hacker link to ICE"/>
        <UrlFieldWithQr name="User URL" type="iceApp" id={id} description="User link to ICE"/>
    </>
}
