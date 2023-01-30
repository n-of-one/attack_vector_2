import React from "react";
import {Pad, zeroPad} from "../../../../../common/component/Pad";

const ScanInfoTimerTrigger =({layer}) => {

    const hours = Math.floor(layer.minutes / 60);
    const minutes = layer.minutes % 60;

    const hoursText = zeroPad(hours, 2);
    const minutesText = zeroPad(minutes, 2);
    const secondsText = zeroPad(layer.seconds, 2);


    return (
        <>
            <br/>
            <Pad length="8" />Time until detection: <span className="text-info">{hoursText}:{minutesText}:{secondsText}</span><br/>
        </>
    );
};

export default ScanInfoTimerTrigger;