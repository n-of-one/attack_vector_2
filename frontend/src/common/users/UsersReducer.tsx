import {SERVER_USER_DETAILS, User} from "./UserReducer";

export const SERVER_RECEIVE_USERS_OVERVIEW = "SERVER_RECEIVE_USERS_OVERVIEW"
export const CLOSE_USER_EDIT = "CLOSE_USER_EDIT"



export const editUserReducer = (state: User | null = null, action: any) => {
        switch (action.type) {
            case SERVER_USER_DETAILS:
                return action.data
            case CLOSE_USER_EDIT:
                return null
            default:
                return state
        }
}


export interface UserOverview {
    id: string,
    name: string,
    characterName?: string,
}


export const userOverviewReducer = (state: Array<UserOverview> = [], action: any) => {

    switch (action.type) {
        case SERVER_RECEIVE_USERS_OVERVIEW:
            return action.data
        default:
            return state
    }
}