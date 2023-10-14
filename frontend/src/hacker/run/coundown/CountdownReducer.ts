import {serverTime} from "../../../common/server/ServerTime";
import {AnyAction} from "redux";
import {TICK} from "../../../common/terminal/TerminalReducer";
import {SERVER_ENTER_RUN} from "../../server/RunServerActionProcessor";
import {NAVIGATE_PAGE} from "../../../common/menu/pageReducer";

export const SERVER_START_COUNTDOWN = "SERVER_START_COUNTDOWN"
export const SERVER_COMPLETE_COUNTDOWN = "SERVER_COMPLETE_COUNTDOWN"
export const SERVER_REMOVE_COUNTDOWN = "SERVER_REMOVE_COUNTDOWN"

export const SERVER_FLASH_PATROLLER = "SERVER_FLASH_PATROLLER"
export const SERVER_START_TRACING_PATROLLER = "SERVER_START_TRACING_PATROLLER"
export const SERVER_PATROLLER_MOVE = "SERVER_PATROLLER_MOVE"
export const SERVER_PATROLLER_LOCKS_HACKER = "SERVER_PATROLLER_LOCKS_HACKER"
export const SERVER_PATROLLER_REMOVE = "SERVER_PATROLLER_REMOVE"


export interface CountdownTimerState {
    timerId: string,
    finishAt: string | null,    // "2019-08-26T15:38:40.9179757+02:00",
    secondsLeft: number | null,
    type: string,
    target: string,
    effect: string
}

export const countdownReducer = (state: CountdownTimerState[] = [], action: AnyAction): CountdownTimerState[] => {

    switch (action.type) {
        case TICK:
            if (!action.newSecond) {
                return state
            }
            return processSecondElapsed(state)
        case SERVER_ENTER_RUN:
            return action.data.countdowns
        case NAVIGATE_PAGE:
            return []
        case SERVER_START_COUNTDOWN:
            return serverStartCountdown(state, action.data)
        case SERVER_COMPLETE_COUNTDOWN:
            return processExpireTimer(state, action.data)
        case SERVER_REMOVE_COUNTDOWN:
            return processRemoveTimer(state, action.data)
        default:
            return state
    }
}

interface CountdownStart {
    timerId: string,
    finishAt: string
    type: string,
    target: string,
    effect: string
}

const serverStartCountdown = (state: CountdownTimerState[], countDownStart: CountdownStart): CountdownTimerState[] => {
    const secondsLeft = serverTime.secondsLeft(countDownStart.finishAt)
    const newTimer: CountdownTimerState = {...countDownStart, secondsLeft: secondsLeft}
    return [...state, newTimer]
}

const processSecondElapsed = (state: CountdownTimerState[]) => {
    const newTimers = state.map(timer => processTickTimer(timer))
    return newTimers
}

const processTickTimer = (state: CountdownTimerState): CountdownTimerState => {
    if (state.secondsLeft === 0 || !state.finishAt) {
        return state
    }
    const secondsLeft = Math.max(0, serverTime.secondsLeft(state.finishAt))
    return {...state, secondsLeft: secondsLeft}
}


interface SpecificTimer {
    countdownId: string
}

const processExpireTimer = (state: CountdownTimerState[], action: SpecificTimer): CountdownTimerState[] => {

    return state.filter(timer => timer.timerId !== action.countdownId)
}

const processRemoveTimer = (state: CountdownTimerState[], action: SpecificTimer) => {
    const newTimers = state.filter(timer => timer.timerId !== action.countdownId)
    return newTimers
}
