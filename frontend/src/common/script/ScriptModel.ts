
export enum ObsoleteScriptType {
    TRIPWIRE_EXTRA_TIME= "TRIPWIRE_EXTRA_TIME",
    DEEP_SCAN= "DEEP_SCAN",
}

export const scriptNames = {
    [ObsoleteScriptType.TRIPWIRE_EXTRA_TIME]: "Tripwire_extra_time",
    [ObsoleteScriptType.DEEP_SCAN] : "Deep_scan",
}

export interface ObsoleteScript {
// FIXME: delete

    id: string,
    type: ObsoleteScriptType,
    name: string,
    code: string,
    timeLeft: string,
    value: string,
    usable: boolean,
}

export enum ScriptState {
    NOT_LOADED = "NOT_LOADED",
    LOADING = "LOADING",
    LOADED = "LOADED",
    USED = "USED",
    EXPIRED = "EXPIRED",
    DELETED = "DELETED",
}

export interface Script {
    id: string,
    name: string,
    effects: string[]
    code: string,
    timeLeft: string,
    state: ScriptState,
    ram: number,
    loaded: boolean,
    loadStartedAt: string,      // "2024-12-01T15:38:40.9179757+02:00",
    loadTimeFinishAt: string,   // "2024-12-01T16:08:40.9179757+02:00",
}
