import {AnyAction} from "redux";
import {TICK} from "../terminal/TerminalReducer";
import {SERVER_RECEIVE_EDIT_USER} from "../users/EditUserDataReducer";
import {SERVER_RECEIVE_CURRENT_USER, User} from "../users/CurrentUserReducer";
import {serverTime} from "../server/ServerTime";


export interface ScriptLoading {
    scriptId: string,
    totalLoadTime: number,
    secondsLeft: number,
}

export const scriptLoadingReducer = (state: ScriptLoading[] = [], action: AnyAction): ScriptLoading[] => {
    switch (action.type) {
        case TICK:
            if (!action.newSecond) {
                return state
            }
            return processSecondElapsed(state)
        case SERVER_RECEIVE_EDIT_USER:
        case SERVER_RECEIVE_CURRENT_USER:
            return deriveScriptLoadingState(action.data)
        default:
            return state
    }
}

const processSecondElapsed = (state: ScriptLoading[]): ScriptLoading[] => {
    const scriptsThatAreLoading = state.filter(scriptLoadingState => scriptLoadingState.secondsLeft > 0)
    if (scriptsThatAreLoading.length === 0) return state

    return state.map(scriptLoadingState => {
        if (scriptLoadingState.secondsLeft === 0) return scriptLoadingState
        const secondsLeft = Math.max(0, scriptLoadingState.secondsLeft - 1)
        return { ...scriptLoadingState, secondsLeft}
    })
}


const deriveScriptLoadingState = (userData: User) => {
    if (!userData.hacker) {
        return []
    }
    return userData.hacker.scripts.map(script => {
        const totalLoadTime = serverTime.seconds(script.loadStartedAt, script.loadTimeFinishAt)
        const secondsLeft = serverTime.secondsLeft(script.loadTimeFinishAt)
        return {
            scriptId: script.id,
            totalLoadTime,
            secondsLeft,
        }
    })
}

