import {AnyAction} from "redux"
import {SERVER_SITE_FULL} from "../server/EditorServerActionProcessor"

export const SELECT_NODE = "SELECT_NODE"

export const currentNodeIdReducer = (state: string | null = null, action: AnyAction) => {
    switch(action.type) {
        case SELECT_NODE : return action.data
        case SERVER_SITE_FULL: return null
        default: return state

    }
}
