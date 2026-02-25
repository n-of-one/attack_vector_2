import {AnyAction} from "redux";
import {HackerIcon} from "./HackerIcon";
import {ConfigState} from "../../admin/config/ConfigReducer";
import {HackerSkill, HackerSkillType} from "./HackerSkills";
import {Page} from "../menu/pageReducer";

export const SERVER_RECEIVE_CURRENT_USER = "SERVER_RECEIVE_CURRENT_USER"

export enum UserType {
    NO_DATA = "NO_DATA",
    HACKER = "HACKER",
    GM = "GM",
    ADMIN = "ADMIN",
    SYSTEM = "SYSTEM",
}

export const UserTypeLabels: Record<UserType, string> = {
    [UserType.HACKER]: "Hacker",
    [UserType.GM]: "GM",
    [UserType.ADMIN]: "Admin",
    [UserType.SYSTEM]: "System",
    [UserType.NO_DATA]: "-",
};

export interface User {
    id: string,
    name: string,
    type: UserType,
    preferences: UserPreferences,
    hacker?: Hacker,
}

export interface UserPreferences {
    fontSize: number,
}

export interface Hacker {
    characterName: string,
    icon: HackerIcon,
    skills: HackerSkill[],
    scriptCredits: number,
    scriptIncomeCollectionStatus: ScriptIncomeCollectionStatus,
}

export enum ScriptIncomeCollectionStatus {
    HACKER_HAS_NO_INCOME = "HACKER_HAS_NO_INCOME",
    TODAY_IS_NOT_AN_INCOME_DATE = "TODAY_IS_NOT_AN_INCOME_DATE",
    AVAILABLE = "AVAILABLE",
    COLLECTED = "COLLECTED",
}

export interface GenericUserRootState {
    currentUser: User,
    currentPage: Page,
    config: ConfigState,
}

const defaultUser = {
    id: "-1",
    name: "",
    email: "",
    type: UserType.NO_DATA,
    preferences: {fontSize: 12},
    hacker: undefined,
    gmNote: "",
}

export const currentUserReducer = (state: User = defaultUser, action: AnyAction): User => {
    switch(action.type) {
        case SERVER_RECEIVE_CURRENT_USER : return action.data
        default: return state
    }
}

export const hasSkill = (user: User, requested: HackerSkillType): boolean => {
    const skills = user.hacker?.skills || []
    return skills.some((hackerSKill) => hackerSKill.type === requested)
}

export const skillValueAsIntOrNull = (user: User, requested: HackerSkillType): number | null => {
    const skills = user.hacker?.skills || []
    const skill = skills.find((hackerSKill) => hackerSKill.type === requested)
    if (!skill || !skill.value) {
        return null
    }
    return parseInt(skill.value)
}
