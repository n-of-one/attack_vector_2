import {AnyAction} from "redux";

export const SERVER_RECEIVE_CONFIG = "SERVER_RECEIVE_CONFIG"
export const SELECT_CONFIG = "SELECT_CONFIG"

export enum ConfigItem {
    HACKER_SHOW_SKILLS = "HACKER_SHOW_SKILLS",
    HACKER_EDIT_USER_NAME = "HACKER_EDIT_USER_NAME",
    HACKER_EDIT_CHARACTER_NAME = "HACKER_EDIT_CHARACTER_NAME",
    HACKER_DELETE_RUN_LINKS = "HACKER_DELETE_RUN_LINKS",
    HACKER_CREATE_SITES = "HACKER_CREATE_SITES",
    LOGIN_PATH = "LOGIN_PATH",
    LOGIN_PASSWORD = "LOGIN_PASSWORD",
    LOGIN_GOOGLE_CLIENT_ID = "LOGIN_GOOGLE_CLIENT_ID",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS = "DEV_SIMULATE_NON_LOCALHOST_DELAY_MS",
    DEV_HACKER_RESET_SITE = "DEV_HACKER_RESET_SITE",
    DEV_QUICK_PLAYING = "DEV_QUICK_PLAYING",
    DEV_HACKER_USE_DEV_COMMANDS = "DEV_HACKER_USE_DEV_COMMANDS",
    LARP_NAME = "LARP_NAME",
    FRONTIER_ORTHANK_TOKEN = "FRONTIER_ORTHANK_TOKEN",
}

export const ConfigItemCategories = {
    HACKER_SHOW_SKILLS : "Hacker",
    HACKER_EDIT_USER_NAME : "Hacker",
    HACKER_EDIT_CHARACTER_NAME : "Hacker",
    HACKER_DELETE_RUN_LINKS : "Hacker",
    HACKER_CREATE_SITES : "Hacker",
    LOGIN_PATH : "Login",
    LOGIN_PASSWORD : "Login",
    LOGIN_GOOGLE_CLIENT_ID : "Login",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS : "Development",
    DEV_HACKER_RESET_SITE : "Development",
    DEV_QUICK_PLAYING : "Development",
    DEV_HACKER_USE_DEV_COMMANDS : "Development",
    LARP_NAME : "Larp",
    FRONTIER_ORTHANK_TOKEN : "Frontier",
}

export const ConfigItemNames = {
    HACKER_SHOW_SKILLS : "Show skills",
    HACKER_EDIT_USER_NAME : "Edit user name",
    HACKER_EDIT_CHARACTER_NAME : "Edit character name",
    HACKER_DELETE_RUN_LINKS : "delete run links",
    HACKER_CREATE_SITES : "Create sites",
    LOGIN_PATH : "Path",
    LOGIN_PASSWORD : "Password",
    LOGIN_GOOGLE_CLIENT_ID : "Google client id",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS : "simulate non-localhost",
    DEV_HACKER_RESET_SITE : "hackers can reset sites",
    DEV_QUICK_PLAYING : "ICE quick playing",
    DEV_HACKER_USE_DEV_COMMANDS : "hackers can use dev commands",
    LARP_NAME : "Name",
    FRONTIER_ORTHANK_TOKEN : "Orthank token",
}


export interface ConfigEntry {
    item: ConfigItem,
    value: string
}

export interface ConfigState {
    entries: ConfigEntry[],
    currentItem: ConfigItem | null
    currentValue: string
}

const defaultState = {
    entries: [{item: ConfigItem.HACKER_SHOW_SKILLS, value: "false"}],
    currentItem: null,
    currentValue: "false"
}

export const configReducer = (state: ConfigState = defaultState, action: AnyAction): ConfigState => {

    switch (action.type) {
        case SERVER_RECEIVE_CONFIG :
            return processServerReceiveConfig(state, action as unknown as ActionReceiveConfig)
        case SELECT_CONFIG:
            return {...state, currentItem: action.entry.item, currentValue: action.entry.value}
        default:
            return state
    }
}

interface ActionReceiveConfig {
    data: ConfigEntry[]
}

const processServerReceiveConfig = (state: ConfigState, action: ActionReceiveConfig): ConfigState => {
    const entries = action.data
    let currentEntry = entries.find((entry) => entry.item === state.currentItem)
    if (currentEntry === undefined) return {...state, entries: entries}

    return {entries: action.data, currentItem: currentEntry.item, currentValue: currentEntry.value}
}
