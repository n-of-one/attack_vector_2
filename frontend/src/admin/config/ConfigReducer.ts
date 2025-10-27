import {AnyAction} from "redux";

export const SERVER_RECEIVE_CONFIG = "SERVER_RECEIVE_CONFIG"
export const SELECT_CONFIG = "SELECT_CONFIG"

export enum ConfigItem {
    LARP_NAME = "LARP_NAME",

    HACKER_DELETE_RUN_LINKS = "HACKER_DELETE_RUN_LINKS",
    HACKER_EDIT_CHARACTER_NAME = "HACKER_EDIT_CHARACTER_NAME",
    HACKER_EDIT_USER_NAME = "HACKER_EDIT_USER_NAME",
    HACKER_SHOW_SKILLS = "HACKER_SHOW_SKILLS",
    HACKER_TUTORIAL_SITE_NAME = "HACKER_TUTORIAL_SITE_NAME",
    HACKER_SCRIPT_RAM_REFRESH_DURATION = "HACKER_SCRIPT_RAM_REFRESH_DURATION",
    HACKER_SCRIPT_LOCKOUT_DURATION = "HACKER_SCRIPT_LOCKOUT_DURATION",
    HACKER_SCRIPT_LOAD_DURING_RUN = "HACKER_SCRIPT_LOAD_DURING_RUN",

    LOGIN_GOOGLE_CLIENT_ID = "LOGIN_GOOGLE_CLIENT_ID",
    LOGIN_PASSWORD = "LOGIN_PASSWORD",
    LOGIN_PATH = "LOGIN_PATH",

    DEV_HACKER_RESET_SITE = "DEV_HACKER_RESET_SITE",
    DEV_HACKER_USE_DEV_COMMANDS = "DEV_HACKER_USE_DEV_COMMANDS",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS = "DEV_SIMULATE_NON_LOCALHOST_DELAY_MS",
    DEV_TESTING_MODE = "DEV_TESTING_MODE",
    DEV_QUICK_PLAYING = "DEV_QUICK_PLAYING",

    LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN = "LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN",
    LARP_SPECIFIC_FRONTIER_LOLA_ENABLED = "LARP_SPECIFIC_FRONTIER_LOLA_ENABLED",
}

export const ConfigItemCategories = {
    LARP_NAME: "1. Generic",

    HACKER_DELETE_RUN_LINKS: "2. Hacker",
    HACKER_EDIT_CHARACTER_NAME: "2. Hacker",
    HACKER_EDIT_USER_NAME: "2. Hacker",
    HACKER_SHOW_SKILLS: "2. Hacker",
    HACKER_TUTORIAL_SITE_NAME: "2. Hacker",
    HACKER_SCRIPT_RAM_REFRESH_DURATION: "2. Hacker",
    HACKER_SCRIPT_LOCKOUT_DURATION: "2. Hacker",
    HACKER_SCRIPT_LOAD_DURING_RUN: "2. Hacker",

    LOGIN_GOOGLE_CLIENT_ID: "3. Login",
    LOGIN_PASSWORD: "3. Login",
    LOGIN_PATH: "3. Login",

    DEV_HACKER_RESET_SITE: "4. Development",
    DEV_HACKER_USE_DEV_COMMANDS: "4. Development",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS: "4. Development",
    DEV_TESTING_MODE: "4. Development",
    DEV_QUICK_PLAYING: "4. Development",

    LARP_SPECIFIC_FRONTIER_LOLA_ENABLED: "5. Larp specific - Frontier",
    LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN: "5. Larp specific - Frontier",

}

export const ConfigItemNames = {
    LARP_NAME: "Larp name",

    HACKER_DELETE_RUN_LINKS: "Delete run links",
    HACKER_EDIT_CHARACTER_NAME: "Edit character name",
    HACKER_EDIT_USER_NAME: "Edit user name",
    HACKER_SHOW_SKILLS: "Show skills",
    HACKER_TUTORIAL_SITE_NAME: "Tutorial site name",
    HACKER_SCRIPT_RAM_REFRESH_DURATION: "Script RAM refresh duration",
    HACKER_SCRIPT_LOCKOUT_DURATION: "Script lockout duration",
    HACKER_SCRIPT_LOAD_DURING_RUN: "Script loading during run",

    LOGIN_GOOGLE_CLIENT_ID: "Google client id",
    LOGIN_PASSWORD: "Password",
    LOGIN_PATH: "Path",

    DEV_HACKER_USE_DEV_COMMANDS: "Hackers can use dev commands",
    DEV_HACKER_RESET_SITE: "Hackers can reset sites",
    DEV_SIMULATE_NON_LOCALHOST_DELAY_MS: "Simulate non-localhost",
    DEV_TESTING_MODE: "Testing mode",
    DEV_QUICK_PLAYING: "ICE quick playing",

    LARP_SPECIFIC_FRONTIER_LOLA_ENABLED: "LOLA enabled",
    LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN: "Orthank token",
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
            const categoryA = ConfigItemCategories[a.item]
            if (categoryA === undefined) {
                console.error("Config item exists on server but not on frontend: " + a.item)
                return -1
            }
            const categoryB = ConfigItemCategories[b.item]
            if (categoryB === undefined) {
                console.error("Config item exists on server but not on frontend " + b.item)
                return 1
            }

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
