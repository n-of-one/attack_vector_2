
export const SERVER_USER_DETAILS = "SERVER_USER_DETAILS"
export const SERVER_RECEIVE_USERS_OVERVIEW = "SERVER_RECEIVE_USERS_OVERVIEW"
export const CLOSE_USER_EDIT = "CLOSE_USER_EDIT"

export const USER_TYPE_HACKER = "HACKER"
export const USER_TYPE_HACKER_MANAGER = "HACKER_MANAGER"
export const USER_TYPE_GM = "GM"
export const USER_TYPE_ADMIN = "ADMIN"


type UserType = "HACKER" | "HACKER_MANAGER" | "GM" | "ADMIN"

export interface User {
    id: string,
    name: string,
    email: string,
    type: UserType,
    hacker?: Hacker,
}

export interface Hacker {
    characterName: string,
    playerName: string,
    skill: HackerSkill,
}

export interface HackerSkill {
    hacker: number,
    elite: number,
    architect: number
}


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
    playerName?: string,
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