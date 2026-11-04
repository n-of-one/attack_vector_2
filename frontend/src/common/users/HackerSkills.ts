export enum HackerSkillType {
    BYPASS = "BYPASS",
    CREATE_SITE = "CREATE_SITE",
    JUMP_TO_HACKER = "JUMP_TO_HACKER",
    SCAN = "SCAN",
    SCRIPT_CREDITS = "SCRIPT_CREDITS",
    SCRIPT_RAM = "SCRIPT_RAM",
    SEARCH_SITE = "SEARCH_SITE",
    ADJUSTED_SPEED = "ADJUSTED_SPEED",
    STEALTH = "STEALTH",
    WEAKEN = "WEAKEN",
    UNDO_TRIPWIRE = "UNDO_TRIPWIRE",
    TRAIT = "TRAIT",
}

export interface HackerSkill {
    id: string,
    type: HackerSkillType,
    value?: string,
}

export const skillName: Record<HackerSkillType, string> = {
    [HackerSkillType.ADJUSTED_SPEED]: "Adjusted speed",
    [HackerSkillType.BYPASS]: "Bypass",
    [HackerSkillType.CREATE_SITE]: "Create site",
    [HackerSkillType.JUMP_TO_HACKER]: "Jump",
    [HackerSkillType.SCAN]: "Scan",
    [HackerSkillType.SCRIPT_CREDITS]: "Script credits (income)",
    [HackerSkillType.SCRIPT_RAM]: "Scripts (RAM)",
    [HackerSkillType.SEARCH_SITE]: "Search site",
    [HackerSkillType.STEALTH]: "Stealth",
    [HackerSkillType.WEAKEN]: "Weaken",
    [HackerSkillType.UNDO_TRIPWIRE]: "Rollback",
    [HackerSkillType.TRAIT]: "Trait",
}

export const skillHasValue: Record<HackerSkillType, boolean> = {
    [HackerSkillType.ADJUSTED_SPEED]: true,
    [HackerSkillType.BYPASS]: false,
    [HackerSkillType.CREATE_SITE]: false,
    [HackerSkillType.JUMP_TO_HACKER]: false,
    [HackerSkillType.SCAN]: false,
    [HackerSkillType.SEARCH_SITE]: false,
    [HackerSkillType.SCRIPT_RAM]: true,
    [HackerSkillType.SCRIPT_CREDITS]: true,
    [HackerSkillType.STEALTH]: true,
    [HackerSkillType.WEAKEN]: true,
    [HackerSkillType.UNDO_TRIPWIRE]: false,
    [HackerSkillType.TRAIT]: true,
}

export const skillInfoText: Record<HackerSkillType, string> = {
    [HackerSkillType.ADJUSTED_SPEED]: "Speed of the text in the terminal, as well as move and start-attack speed. Default speed is [DEFAULT_SPEED].",
    [HackerSkillType.BYPASS]: "The hacker can ignore the ICE in the entry node.",
    [HackerSkillType.CREATE_SITE]: "The hacker can create their own sites.",
    [HackerSkillType.JUMP_TO_HACKER]: "The hacker can 'jump' to a node where another hacker is present. Not blocked by ICE along the way.",
    [HackerSkillType.SEARCH_SITE]: "The hacker can search for sites and start a hacking run. Without this skill, the hacker always needs another hacker find " +
    "the site and invite them to the run.",
    [HackerSkillType.SCAN]: "The hacker can use the 'scan' command.",
    [HackerSkillType.SCRIPT_CREDITS]: "The hacker can steal data and sell it to a data broker for script credits (⚡). Credits can be used to buy scripts." +
    " The value is the amount of script credits the hacker can collect each day as a passive income.",
    [HackerSkillType.SCRIPT_RAM]: "The hacker can 'run' scripts. Without this skill the hacker cannot interact with scripts in any way. The value is the amount " +
    "of RAM available for scripts.",
    [HackerSkillType.STEALTH]: "The hacker can will increase tripwire timer durations by this percentage (or decrease them if the percentage is negative).",
    [HackerSkillType.WEAKEN]: "The hacker can 'weaken' ICE, this reduces the strength of an ICE layer. The value defines which ICE types can be affected. Each instance " +
    "of this skill can be used once per site.",
    [HackerSkillType.UNDO_TRIPWIRE]: "The hacker can use 'rollback' to retreat from a node they just moved into," +
        " returning to the previous node and undoing any tripwire they triggered in the process.",
    [HackerSkillType.TRAIT]: "A trait of the hacker character that has meaning in the game, for example a background, an implant or a reputation." +
        " GMs and players can look it up and react to it, and sites can react to it as well, for example by showing a message when entering" +
        " a node (Daemon layer). The key identifies the trait, the text is what the hacker sees in their list of skills.",
}

export const skillCanHaveMultipleInstances: Record<HackerSkillType, boolean> = {
    [HackerSkillType.ADJUSTED_SPEED]: false,
    [HackerSkillType.BYPASS]: false,
    [HackerSkillType.CREATE_SITE]: false,
    [HackerSkillType.JUMP_TO_HACKER]: false,
    [HackerSkillType.SCAN]: false,
    [HackerSkillType.SCRIPT_CREDITS]: false,
    [HackerSkillType.SCRIPT_RAM]: false,
    [HackerSkillType.SEARCH_SITE]: false,
    [HackerSkillType.STEALTH]: false,
    [HackerSkillType.WEAKEN]: true,
    [HackerSkillType.UNDO_TRIPWIRE]: false,
    [HackerSkillType.TRAIT]: true,
}

/** The value of a TRAIT skill is "<key>: <text>". The hacker only gets to see the text. */
export const splitTraitSkillValue = (value: string | undefined): { key: string, text: string } => {
    if (!value) return {key: '', text: ''}
    const separatorIndex = value.indexOf(':')
    if (separatorIndex === -1) return {key: value.trim(), text: ''}
    return {key: value.substring(0, separatorIndex).trim(), text: value.substring(separatorIndex + 1).trim()}
}

export const joinTraitSkillValue = (key: string, text: string): string => `${key.trim()}: ${text.trim()}`
