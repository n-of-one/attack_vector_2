import React from 'react';
import { useSelector} from "react-redux";
import {HackerState} from "../../HackerRootReducer";
import {formatTimeInterval} from "../../../common/Util";

export const CountdownTimer = () => {

    const showCountdown = useSelector((state: HackerState) => state.run.countdown.finishAt !== null)
    const secondsLeft = useSelector((state: HackerState) => state.run.countdown.secondsLeft)

    if (showCountdown) {
        return (
            <span className="countdown">{formatTimeInterval(secondsLeft)}</span>
        );
    } else {
        return <></>;
    }
}
