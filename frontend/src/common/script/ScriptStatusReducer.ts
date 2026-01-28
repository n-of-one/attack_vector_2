import { AnyAction } from "redux";
import {Script} from "./ScriptModel";
import {serverTime} from "../server/ServerTime";
import {TICK} from "../terminal/TerminalReducer";

export const SERVER_RECEIVE_SCRIPT_STATUS = "SERVER_RECEIVE_SCRIPT_STATUS"

export interface ScriptStatus {
    scripts: Script[],
    ram: Ram,
}

export interface Ram {
    enabled: boolean,
    size: number,
    loaded: number,
    refreshing: number,
    free: number,
    nextRefreshAt: string,
    nextRefreshSecondsLeft: number | null,
    lockedUntil: string | null,
}

export const scriptStatusReducer = (state: ScriptStatus|null = null, action: AnyAction): ScriptStatus|null => {
    switch (action.type) {
        case TICK:
            if (!action.newSecond) {
                return state
            }
            return processSecondElapsed(state)
        case SERVER_RECEIVE_SCRIPT_STATUS:
            return {scripts: action.data.scripts, ram: toRam(action.data.ram)}
        default:
            return state
    }
}

interface RamUpdate  {
    enabled: boolean,
    size: number,
    loaded: number,
    refreshing: number,
    free: number,
    nextRefreshAt: string,
    lockedUntil: string | null,
}

const toRam = (data: RamUpdate): Ram => {
    const nextRefreshSeconds = (data.nextRefreshAt === null) ? null : serverTime.secondsLeft(data.nextRefreshAt)
    return {
        enabled : data.enabled,
        size : data.size,
        loaded : data.loaded,
        refreshing : data.refreshing,
        free : data.free,
        lockedUntil : data.lockedUntil,
        nextRefreshAt: data.nextRefreshAt,
        nextRefreshSecondsLeft : nextRefreshSeconds
    }
}

const processSecondElapsed = (state: ScriptStatus|null): ScriptStatus|null => {
    if (state === null || state.ram == null || state.ram.nextRefreshSecondsLeft === null) {
        return state
    }
    const secondsLeft = Math.max(0, serverTime.secondsLeft(state.ram.nextRefreshAt))
    const updatedRam = {...state.ram, nextRefreshSecondsLeft: secondsLeft}


    return {...state, ram: updatedRam}
}
