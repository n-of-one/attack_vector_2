import {AnyAction} from "redux";
import {HackerIcon} from "./HackerIcon";
import {ConfigState} from "../../admin/config/ConfigReducer";

export const SERVER_RECEIVE_CURRENT_USER = "SERVER_RECEIVE_CURRENT_USER"

export enum UserType {
    NO_DATA = "NO_DATA",
    HACKER = "HACKER",
    GM = "GM",
    ADMIN = "ADMIN",
    SYSTEM = "SYSTEM",
}

export interface User {
    id: string,
    name: string,
    type: UserType,
    hacker?: Hacker,
}

export const hasSkill = ( user: User, requested: HackerSkillType): boolean => {
    const skills = user.hacker?.skills || []
    return skills.some((hackerSKill) => hackerSKill.type === requested)
}

export enum HackerSkillType {
    SEARCH_SITE = "SEARCH_SITE",
    SCAN = "SCAN",
    CREATE_SITE = "CREATE_SITE",
    // STEALTH = "STEALTH", // Await confirmation from the organisers who use the game that this is a skill they want.
    SCRIPT_RAM = "SCRIPT_RAM",
}

export interface HackerSkill {
    type: HackerSkillType,
    value?: string,
}

export interface Hacker {
    characterName: string,
    icon: HackerIcon,
    skills: HackerSkill[],
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
        case SERVER_RECEIVE_CURRENT_USER : return action.data
        default: return state
    }
}
