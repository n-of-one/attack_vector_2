import {ObsoleteScriptType} from "./ScriptModel";

export interface HackerScriptAccess {
    id: string,
    ownerUserId: string,
    type: ObsoleteScriptType,
    name: string,
    value: string | null,
    ram: number,
    receiveForFree: number,
    price: number | null,
}

