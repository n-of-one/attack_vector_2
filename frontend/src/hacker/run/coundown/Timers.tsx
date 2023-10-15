import React from 'react';
import {useSelector} from "react-redux";
import {HackerState} from "../../HackerRootReducer";
import {formatTimeInterval} from "../../../common/util/Util";
import {TimerState, TimerType} from "./TimersReducer";

export const Timers = () => {

    const timers: TimerState[] = useSelector((state: HackerState) => state.run.timers)

    if (timers.length === 0) {
        return <div>
            <EmptyTimerDisplay />
            <Divider />
        </div>;
    }

    return <div>
        {timers.map((timer: TimerState) => <TimerDisplay key={timer.timerId} {...timer} />)}
        <Divider />
    </div>
}

const TimerDisplay = (props: TimerState) => {

    const textColor = props.type === TimerType.SHUTDOWN_START ? "text-warning" : "text-info"

    return <div className="row">
        <div className="col-lg-12">
            <span className="text">
                <span className={`timer ${textColor}`}>{formatTimeInterval(props.secondsLeft)}</span>
                &nbsp;{typeText(props)} [{props.target}] ➜ {props.effect}
            </span>
        </div>
    </div>
}

const typeText = (timer: TimerState) => {
    switch (timer.type) {
        case TimerType.SHUTDOWN_START: return "tripwire"
        case TimerType.SHUTDOWN_FINISH: return "shutdown"
        default: return ""
    }
}

const EmptyTimerDisplay = () => {

    return <div className="row">
        <div className="col-lg-12">
            <span className="text">
                <span className="timer">&nbsp;</span>
            </span>
        </div>
    </div>
}

const Divider = () => {
    return <div><hr style={{"borderColor": "#666", "margin": "10px 0px 6px 0px" }}/></div>
}