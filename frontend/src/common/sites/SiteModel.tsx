import {LayerType} from "../enums/LayerTypes";
import {NodeScanStatus} from "../enums/NodeStatus";

export const TRANSIT_1 = "transit_1"
export const TRANSIT_2 = "transit_2"
export const TRANSIT_3 = "transit_3"
export const TRANSIT_4 = "transit_4"
export const SYSCON = "syscon"
export const DATA_STORE = "data_store"
export const PASSCODE_STORE = "passcode_store"
export const RESOURCE_STORE = "resource_store"
export const ICE_1= "ice_1"
export const ICE_2 = "ice_2"
export const ICE_3 = "ice_3"
export const UNHACKABLE = "unhackable"
export const MANUAL_1 = "manual_1"
export const MANUAL_2 = "manual_2"
export const MANUAL_3 = "manual_3"

export type NodeTypeName = "transit_1" | "transit_2" | "transit_3" | "transit_4" | "syscon" | "data_store" | "passcode_store" | "resource_store" | "ice_1" | "ice_2" | "ice_3" | "unhackable" | "manual_1" | "manual_2" | "manual_3"


export interface LayerDetails {
    id: string
    type: LayerType
    level: number       // height of the layer. level 0 is always OS. Hack top level first.
    name: string
    note: string
    ice: boolean
    hacked: boolean
    original: LayerDetails | null

    nodeName?: string                 // OS layer
    text?: string                     // Text layer
    shutdown?: string                 // Layer Trip wire
    countdown?: string                // Layer Trip wire
    coreLayerId?: string              // Layer Trip wire
    coreSiteId?: string             // Layer Trip wire

    strength? : "VERY_WEAK" | "WEAK" | "AVERAGE" | "STRONG" | "VERY_STRONG" | "ONYX"
    password? : string  // Password Ice layer
    hint?: string       // password Ice layer

    totalUnits?: number          // Tar Ice layer
    time1Level1Hacker?: string   // Tar Ice layer
    time1Level5Hacker?: string   // Tar Ice layer
    time5Level10Hackers?: string // Tar Ice layer

    appId?: string               // Status Light layer
    status?: boolean             // Status Light layer
    textForGreen?: string        // Status Light layer
    textForRed?: string          // Status Light layer

    iceLayerId?: string          // Keystore layer

    revealNetwork?: boolean      // Core layer

    interactionKey?: string      // Script Interaction layer
    message?: string             // Script Interaction layer
}

export interface NodeI {
    id: string,
    siteId: string,
    type: NodeTypeName,
    x: number,
    y: number,
    ice: boolean,
    layers: Array<LayerDetails>,
    networkId: string

    hacked : boolean
    status: NodeScanStatus
    distance: number
}
