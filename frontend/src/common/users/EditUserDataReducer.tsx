import {User} from "./CurrentUserReducer";

export const SERVER_RECEIVE_USERS_OVERVIEW = "SERVER_RECEIVE_USERS_OVERVIEW"
export const CLOSE_USER_EDIT = "CLOSE_USER_EDIT"
export const SERVER_RECEIVE_EDIT_USER = "SERVER_RECEIVE_EDIT_USER"


export const editUserDataReducer = (state: User | null = null, action: any): User | null => {
        switch (action.type) {
            case SERVER_RECEIVE_EDIT_USER:
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
    hacker: boolean,
}


export const userOverviewReducer = (state: Array<UserOverview> = [], action: any) => {

    switch (action.type) {
        case SERVER_RECEIVE_USERS_OVERVIEW:
            return action.data
        default:
            return state
    }
}
