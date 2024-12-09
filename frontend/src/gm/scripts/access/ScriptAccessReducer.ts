import {AnyAction} from "redux";

const SERVER_RECEIVE_SCRIPT_ACCESS = "SERVER_RECEIVE_SCRIPT_ACCESS"

export interface ScriptAccess {
    id: string,
    type: ScriptTypeForAccess,
    receiveForFree: number,
    price: number | null,
    used: boolean,
}

export interface ScriptTypeForAccess {
    id: string,
    name: string,
    ram: number,
    effects: string[]
}

export const scriptAccessReducer = (state: ScriptAccess[] = [], action: AnyAction): ScriptAccess[] => {
    switch (action.type) {
        case SERVER_RECEIVE_SCRIPT_ACCESS:
            return action.data
        default:
            return state
    }
}
