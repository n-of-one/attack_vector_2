import {AnyAction} from "redux";
import {HackerIcon} from "./HackerIcon";
import {ConfigState} from "../../admin/config/ConfigReducer";

export const SERVER_USER_DETAILS = "SERVER_USER_DETAILS"

export enum UserType {
    NO_DATA = "NO_DATA",
    HACKER = "HACKER",
    GM = "GM",
    ADMIN = "ADMIN",
    SKILL_TEMPLATE = "SKILL_TEMPLATE",
}

export interface User {
    id: string,
    name: string,
    type: UserType,
    hacker?: Hacker,
}

export enum HackerSkill {
    SEARCH_SITE = "SEARCH_SITE",
    SCAN = "SCAN",
    CREATE_SITE = "CREATE_SITE",
}

export interface Hacker {
    characterName: string,
    icon: HackerIcon,
    skills: HackerSkill[] | null,
}

export interface GenericUserRootState {
    currentUser: User,
    config: ConfigState,
}

const defaultUser = {
    id: "-1",
    name: "",
    email: "",
    type: UserType.NO_DATA,
    hacker: undefined,
    gmNote: "",
}

export const currentUserReducer = (state: User = defaultUser, action: AnyAction): User => {
    switch(action.type) {
        case SERVER_USER_DETAILS : return action.data
        default: return state
    }
}
