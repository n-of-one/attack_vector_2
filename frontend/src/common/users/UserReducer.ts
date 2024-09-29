import {AnyAction} from "redux";
import {HackerIcon} from "./HackerIcon";

export const SERVER_USER_DETAILS = "SERVER_USER_DETAILS"

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
    gmNote: string,
}

export enum HackerSkill {
    SEARCH_SITE = "SEARCH_SITE",
    SCAN = "SCAN",
}

export interface Hacker {
    characterName: string,
    icon: HackerIcon,
    skills: HackerSkill[] | null,
}

export interface GenericUserRootState {
    currentUser: User | null,
}

export const currentUserReducer = (state: User | null = null, action: AnyAction): User | null => {
    switch(action.type) {
        case SERVER_USER_DETAILS : return action.data
        default: return state
    }
}
