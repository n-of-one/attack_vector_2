import React from "react";
import Pad from "../../../../../common/component/Pad";

export default ({service}) => {

    // FIXME: const hackedText = service.hacked ? "Yes" : "No";
    const hackedText = "(Never, FIXME)";
    return (
        <>
            &nbsp;ICE (static password)<br/>
            <Pad p="8" />Strength: <span className="text-info">unknown</span><br/>
            <Pad p="8" />Hacked: <span className="text-danger">{hackedText}</span>
        </>
    );
};
