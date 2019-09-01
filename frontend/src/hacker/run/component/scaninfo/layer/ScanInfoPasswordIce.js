import React from "react";
import Pad from "../../../../../common/component/Pad";

export default ({layer}) => {

    const hackedText = layer.hacked ? "Yes" : "No";
    return (
        <>
            &nbsp;ICE (static password)<br/>
            <Pad p="8" />Strength: <span className="text-info">unknown</span><br/>
            <Pad p="8" />Hacked: <span className="text-danger">{hackedText}</span>
        </>
    );
};
