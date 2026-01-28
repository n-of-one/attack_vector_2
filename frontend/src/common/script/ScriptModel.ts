

export enum ScriptState {
    AVAILABLE = "AVAILABLE",
    LOADED = "LOADED",
    USED = "USED",
    EXPIRED = "EXPIRED",
    OFFERING = "OFFERING",
}

export interface Script {
    id: string,
    name: string,
    effects: ScriptEffectDisplay[]
    code: string,
    timeLeft: string,
    state: ScriptState,
    ram: number,
    loaded: boolean,
    loadStartedAt: string,      // "2024-12-01T15:38:40.9179757+02:00",
    loadTimeFinishAt: string,   // "2024-12-01T16:08:40.9179757+02:00",
}

export interface ScriptEffectDisplay {
    description: string,
    label: string,
}

