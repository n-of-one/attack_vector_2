import {serverTime} from "../../../common/server/ServerTime";
import {AnyAction} from "redux";
import {TICK} from "../../../common/terminal/TerminalReducer";
import {SERVER_ENTERED_RUN} from "../../RunServerActionProcessor";
import {NAVIGATE_PAGE} from "../../../common/menu/pageReducer";

export const SERVER_START_TIMER = "SERVER_START_TIMER"
export const SERVER_COMPLETE_TIMER = "SERVER_COMPLETE_TIMER"

export const SERVER_FLASH_PATROLLER = "SERVER_FLASH_PATROLLER"

export enum TimerType {
    SHUTDOWN_START = "SHUTDOWN_START",
    SHUTDOWN_FINISH = "SHUTDOWN_FINISH",
}

export interface TimerState {
    timerId: string,
    finishAt: string | null,    // "2019-08-26T15:38:40.9179757+02:00",
    secondsLeft: number | null,
    type: TimerType,
    target: string,
    effect: string
}

export const timersReducer = (state: TimerState[] = [], action: AnyAction): TimerState[] => {

    switch (action.type) {
        case TICK:
            if (!action.newSecond) {
                return state
            }
            return processSecondElapsed(state)
        case SERVER_ENTERED_RUN:
            return action.data.timers
        case NAVIGATE_PAGE:
            return []
        case SERVER_START_TIMER:
            return startTimer(state, action.data)
        case SERVER_COMPLETE_TIMER:
            return completeTimer(state, action.data)
        default:
            return state
    }
}

interface TimerStart {
    timerId: string,
    finishAt: string
    type: TimerType,
    target: string,
    effect: string
}

const startTimer = (state: TimerState[], timerStart: TimerStart): TimerState[] => {
    const secondsLeft = serverTime.secondsLeft(timerStart.finishAt)
    const newTimer: TimerState = {...timerStart, secondsLeft: secondsLeft}
    return [...state, newTimer]
}

const processSecondElapsed = (state: TimerState[]) => {
    const newTimers = state.map(timer => processTickTimer(timer))
    return newTimers
}

const processTickTimer = (state: TimerState): TimerState => {
    if (state.secondsLeft === 0 || !state.finishAt) {
        return state
    }
    const secondsLeft = Math.max(0, serverTime.secondsLeft(state.finishAt))
    return {...state, secondsLeft: secondsLeft}
}


interface SpecificTimer {
    timerId: string
}

const completeTimer = (state: TimerState[], action: SpecificTimer): TimerState[] => {
    return state.filter(timer => timer.timerId !== action.timerId)
}
