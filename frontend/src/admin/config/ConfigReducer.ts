import {AnyAction} from "redux";

export const SERVER_RECEIVE_CONFIG = "SERVER_RECEIVE_CONFIG"
export const SELECT_CONFIG = "SELECT_CONFIG"

export enum ConfigItem {
    HACKER_SHOW_SKILLS = "HACKER_SHOW_SKILLS",
    HACKER_EDIT_USER_NAME = "HACKER_EDIT_USER_NAME",
    HACKER_EDIT_CHARACTER_NAME = "HACKER_EDIT_CHARACTER_NAME",
    HACKER_DELETE_RUN_LINKS = "HACKER_DELETE_RUN_LINKS",
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
    HACKER_SHOW_SKILLS: "Hacker",
    HACKER_EDIT_USER_NAME: "Hacker",
    HACKER_EDIT_CHARACTER_NAME: "Hacker",
    HACKER_DELETE_RUN_LINKS: "Hacker",
    LOGIN_PATH: "Login",
    LOGIN_PASSWORD: "Login",
    LOGIN_GOOGLE_CLIENT_ID: "Login",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS: "Development",
    DEV_HACKER_RESET_SITE: "Development",
    DEV_QUICK_PLAYING: "Development",
    DEV_HACKER_USE_DEV_COMMANDS: "Development",
    LARP_NAME: "Larp",
    FRONTIER_ORTHANK_TOKEN: "Frontier",
}

export const ConfigItemNames = {
    HACKER_SHOW_SKILLS: "Show skills",
    HACKER_EDIT_USER_NAME: "Edit user name",
    HACKER_EDIT_CHARACTER_NAME: "Edit character name",
    HACKER_DELETE_RUN_LINKS: "Delete run links",
    LOGIN_PATH: "Path",
    LOGIN_PASSWORD: "Password",
    LOGIN_GOOGLE_CLIENT_ID: "Google client id",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS: "Simulate non-localhost",
    DEV_HACKER_RESET_SITE: "Hackers can reset sites",
    DEV_QUICK_PLAYING: "ICE quick playing",
    DEV_HACKER_USE_DEV_COMMANDS: "Hackers can use dev commands",
    LARP_NAME: "Name",
    FRONTIER_ORTHANK_TOKEN: "Orthank token",
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
    sortEntries(entries)

    let currentEntry = entries.find((entry) => entry.item === state.currentItem)
    if (currentEntry === undefined) return {...state, entries: entries}

    return {entries: action.data, currentItem: currentEntry.item, currentValue: currentEntry.value}
}

const sortEntries = (entries: ConfigEntry[]) => {
    entries.sort((a, b) => {

            if (a.item === ConfigItem.LARP_NAME && b.item !== ConfigItem.LARP_NAME) return -1
            if (b.item === ConfigItem.LARP_NAME && a.item !== ConfigItem.LARP_NAME) return 1

            const categoryA = ConfigItemCategories[a.item]
            const categoryB = ConfigItemCategories[b.item]
            const categoryComparison = categoryA.localeCompare(categoryB)
            if (categoryComparison !== 0) return categoryComparison

            const nameA = ConfigItemNames[a.item]
            const nameB = ConfigItemNames[b.item]
            return nameA.localeCompare(nameB)
        }
    )
}

// For accessing config from every rootState:

export interface ConfigRootState {
    config: ConfigState
}

// for convenience

export const getConfigAsBoolean = (item: ConfigItem, state: ConfigState,): boolean => {
    const entry = state.entries.find((entry) => entry.item === item)
    return (entry !== undefined) ? entry.value.toUpperCase() === "TRUE" : false
}
