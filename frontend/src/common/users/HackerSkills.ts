export enum HackerSkillType {
    SEARCH_SITE = "SEARCH_SITE",
    SCAN = "SCAN",
    CREATE_SITE = "CREATE_SITE",
    STEALTH = "STEALTH",
    SCRIPT_RAM = "SCRIPT_RAM",
    BYPASS = "BYPASS",
    WEAKEN = "WEAKEN",
    UNDO_TRIPWIRE = "UNDO_TRIPWIRE",
    JUMP_TO_HACKER= "JUMP_TO_HACKER",
}

export interface HackerSkill {
    id: String,
    type: HackerSkillType,
    value?: string,
}

export const skillName: Record<HackerSkillType, string> = {
    [HackerSkillType.SEARCH_SITE]: "Search site",
    [HackerSkillType.SCAN]: "Scan",
    [HackerSkillType.CREATE_SITE]: "Create site",
    [HackerSkillType.SCRIPT_RAM]: "Scripts (RAM)",
    [HackerSkillType.STEALTH]: "Stealth",
    [HackerSkillType.BYPASS]: "Bypass",
    [HackerSkillType.WEAKEN]: "Weaken",
    [HackerSkillType.UNDO_TRIPWIRE]: "Glitch",
    [HackerSkillType.JUMP_TO_HACKER]: "Jump",
}
export const skillHasValue: Record<HackerSkillType, boolean> = {
    [HackerSkillType.SEARCH_SITE]: false,
    [HackerSkillType.SCAN]: false,
    [HackerSkillType.CREATE_SITE]: false,
    [HackerSkillType.SCRIPT_RAM]: true,
    [HackerSkillType.STEALTH]: false,
    [HackerSkillType.BYPASS]: false,
    [HackerSkillType.WEAKEN]: true,
    [HackerSkillType.UNDO_TRIPWIRE]: false,
    [HackerSkillType.JUMP_TO_HACKER]: false,
}

export const skillInfoText: Record<HackerSkillType, string> = {
    [HackerSkillType.SEARCH_SITE]: "The hacker can search for sites and start a hacking run. Without this skill, the hacker always needs another hacker find " +
    "the site and invite them to the run.",
    [HackerSkillType.SCAN]: "The hacker can use the scan command.",
    [HackerSkillType.CREATE_SITE]: "The hacker can create their own sites.",
    [HackerSkillType.SCRIPT_RAM]: "The hacker can run scripts. Without this skill the hacker cannot interact with scripts in any way. The value is the amount " +
    "of RAM available for scripts.",
    [HackerSkillType.STEALTH]: "The hacker can will increase tripwire timer durations by this percentage (or decrease them if the percentage is negative).",
    [HackerSkillType.BYPASS]: "The hacker can ignore the ICE in the start node to move further into the site.",
    [HackerSkillType.WEAKEN]: "The hacker can reduce the strength of an ICE layer. The value defines which ICE types can be affected. Each instance " +
    "of this skill can be used once per site. Multiple instances of this skill are possible.",
    [HackerSkillType.UNDO_TRIPWIRE]: "The hacker can back away from a node they just moved into (moving back to the node they came from)," +
    " undoing tripping any tripwire they just tripped.",
    [HackerSkillType.JUMP_TO_HACKER]: "The hacker can jump to a node where another hacker is present. Not blocked by ICE along the way.",
}
export const skillCanHaveMultipleInstances: Record<HackerSkillType, boolean> = {
    [HackerSkillType.SEARCH_SITE]: false,
    [HackerSkillType.SCAN]: false,
    [HackerSkillType.CREATE_SITE]: false,
    [HackerSkillType.SCRIPT_RAM]: false,
    [HackerSkillType.STEALTH]: false,
    [HackerSkillType.BYPASS]: false,
    [HackerSkillType.WEAKEN]: true,
    [HackerSkillType.UNDO_TRIPWIRE]: false,
    [HackerSkillType.JUMP_TO_HACKER]: false,
}
