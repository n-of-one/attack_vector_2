/** How often a layer that reacts to a hacker entering its node is triggered (e.g. Timer adjuster, Daemon). */
export enum TriggerRecurrence {
    FIRST_ENTRY_ONLY = "FIRST_ENTRY_ONLY",
    EVERY_ENTRY = "EVERY_ENTRY",
    EACH_HACKER_ONCE = "EACH_HACKER_ONCE",
}
