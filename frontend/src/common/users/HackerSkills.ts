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
}

export const skillInfoText: Record<HackerSkillType, string> = {
    [HackerSkillType.ADJUSTED_SPEED]: "Speed of the text in the terminal, as well as move and start-attack speed. Default speed is [DEFAULT_SPEED].",
    [HackerSkillType.BYPASS]: "The hacker can ignore the ICE in the start node to move further into the site.",
    [HackerSkillType.CREATE_SITE]: "The hacker can create their own sites.",
    [HackerSkillType.JUMP_TO_HACKER]: "The hacker can jump to a node where another hacker is present. Not blocked by ICE along the way.",
    [HackerSkillType.SEARCH_SITE]: "The hacker can search for sites and start a hacking run. Without this skill, the hacker always needs another hacker find " +
    "the site and invite them to the run.",
    [HackerSkillType.SCAN]: "The hacker can use the scan command.",
    [HackerSkillType.SCRIPT_CREDITS]: "The hacker can steal data and sell it to a data broker for script credits (⚡). Credits can be used to buy scripts." +
    " The value is the amount of script credits the hacker will receive each day as a passive income.",
    [HackerSkillType.SCRIPT_RAM]: "The hacker can run scripts. Without this skill the hacker cannot interact with scripts in any way. The value is the amount " +
    "of RAM available for scripts.",
    [HackerSkillType.STEALTH]: "The hacker can will increase tripwire timer durations by this percentage (or decrease them if the percentage is negative).",
    [HackerSkillType.WEAKEN]: "The hacker can reduce the strength of an ICE layer. The value defines which ICE types can be affected. Each instance " +
    "of this skill can be used once per site. Multiple instances of this skill are possible.",
    [HackerSkillType.UNDO_TRIPWIRE]: "The hacker can back away from a node they just moved into (moving to the node they came from)," +
    " undoing tripping any tripwire they just tripped.",
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
}
