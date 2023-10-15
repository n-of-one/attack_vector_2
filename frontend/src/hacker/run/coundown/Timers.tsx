import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../HackerRootReducer";
import {formatTimeInterval} from "../../../common/util/Util";
import {TimerState} from "./TimersReducer";

export const Timers = () => {

    const timers: TimerState[] = useSelector((state: HackerState) => state.run.timers)

    if (timers.length === 0) {
        return <span>&nbsp;</span>;
    }

    return <div>
        {timers.map((timer: TimerState) => <TimerDisplay key={timer.timerId} {...timer} />)}
        <div><hr style={{"borderColor": "#666", "margin": "6px 0px 6px 0px" }}/></div>
    </div>
}


const TimerDisplay = (props: TimerState) => {
    return <div className="row">
        <div className="col-lg-12">
            <span className="text">
                <span className="timer">{formatTimeInterval(props.secondsLeft)}</span>
                &nbsp;{props.type} [{props.target}] -&gt; {props.effect}
            </span>
        </div>
    </div>
}