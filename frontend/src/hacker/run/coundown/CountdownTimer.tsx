import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../HackerRootReducer";
import {formatTimeInterval} from "../../../common/util/Util";
import {CountdownTimerState} from "./CountdownReducer";

export const CountdownTimer = () => {

    const timers: CountdownTimerState[] = useSelector((state: HackerState) => state.run.timers)

    if (timers.length === 0) {
        return <span>&nbsp;</span>;
    }

    return <div>
        {timers.map((timer: CountdownTimerState) => <TimerDisplay key={timer.timerId} {...timer} />)}
        <div><hr style={{"borderColor": "#666", "margin": "6px 0px 6px 0px" }}/></div>
    </div>
}


const TimerDisplay = (props: CountdownTimerState) => {
    return <div className="row">
        <div className="col-lg-12">
            <span className="text">
                <span className="countdown">{formatTimeInterval(props.secondsLeft)}</span>
                &nbsp;{props.type} [{props.target}] -&gt; {props.effect}
            </span>
        </div>
    </div>
}