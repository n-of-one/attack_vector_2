import React from "react";
import Pad from "../../../../../common/component/Pad";

export default ({layer, iceDescription}) => {

    const hackedText = layer.hacked ? "Yes" : "No";
    return (
        <>
            &nbsp;ICE ({iceDescription})<br/>
            <Pad p="8" />Strength: <span className="text-info">{layer.strength}</span><br/>
            <Pad p="8" />Hacked: <span className="text-danger">{hackedText}</span>
        </>
    );
};
