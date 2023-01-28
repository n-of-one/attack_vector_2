import React from 'react';
import { useSelector} from "react-redux";
import serverTime from "../../../common/ServerTime";
import {HackerState} from "../../HackerRootReducer";

export const CountdownTimer = () => {

    const showCountdown = useSelector((state: HackerState) => state.run.countdown.finishAt !== null)
    const secondsLeft = useSelector((state: HackerState) => state.run.countdown.secondsLeft)

    if (showCountdown) {
        return (
            <span className="countdown">{serverTime.format(secondsLeft)}</span>
        );
    } else {
        return <></>;
    }
}
