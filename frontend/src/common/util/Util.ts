import {zeroPad} from "../component/Pad";
import {TICK_MILLIS} from "./Schedule";

export const delay = (toRun: () => void) => {
    setTimeout(toRun, 1)
}

export const delayTicks = (tickCount: number, toRun: () => void) => {
    setTimeout(toRun, TICK_MILLIS * tickCount)
}


export const formatTimeInterval = (totalSecondsLeft: number | null) => {
    if (!totalSecondsLeft) {
        return "--:--:--"
    }
    const waitHours = Math.floor(totalSecondsLeft / (60 * 60));
    const secondsLeftForMinutes = totalSecondsLeft % (60 * 60);
    const waitMinutes = Math.floor(secondsLeftForMinutes / 60);
    const waitSeconds = secondsLeftForMinutes % 60;

    return zeroPad(waitHours, 2) + ":" + zeroPad(waitMinutes, 2) + ":" + zeroPad(waitSeconds, 2);
}

export function notEmpty<TValue>(value: TValue | null | undefined): value is TValue {
    return value !== null && value !== undefined
}

export function hashCode(str: string): number {
    var h: number = 0;
    for (var i = 0; i < str.length; i++) {
        h = 31 * h + str.charCodeAt(i);
    }
    return h & 0xFFFFFFFF
}



