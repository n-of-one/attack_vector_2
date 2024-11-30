
export enum ObsoleteScriptType {
    TRIPWIRE_EXTRA_TIME= "TRIPWIRE_EXTRA_TIME",
    DEEP_SCAN= "DEEP_SCAN",
}

export const scriptNames = {
    [ObsoleteScriptType.TRIPWIRE_EXTRA_TIME]: "Tripwire_extra_time",
    [ObsoleteScriptType.DEEP_SCAN] : "Deep_scan",
}

export interface Script {
    id: string,
    type: ObsoleteScriptType,
    name: string,
    code: string,
    timeLeft: string,
    value: string,
    usable: boolean,
}


