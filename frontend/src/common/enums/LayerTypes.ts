export enum LayerType {
    OS = "OS",
    TEXT= "TEXT",
    LOCK = "LOCK",
    STATUS_LIGHT = "STATUS_LIGHT",
    KEYSTORE = "KEYSTORE",
    TRIPWIRE = "TRIPWIRE",
    TIMER_ADJUSTER = "TIMER_ADJUSTER",
    CORE = "CORE",
    SCRIPT_INTERACTION = "SCRIPT_INTERACTION",
    SCRIPT_CREDITS = "SCRIPT_CREDITS",

    PASSWORD_ICE = "PASSWORD_ICE",
    TANGLE_ICE = "TANGLE_ICE",
    WORD_SEARCH_ICE = "WORD_SEARCH_ICE",
    NETWALK_ICE = "NETWALK_ICE",
    TAR_ICE = "TAR_ICE",
    SWEEPER_ICE = "SWEEPER_ICE",
}

export enum IceType {
    PASSWORD_ICE = "PASSWORD_ICE",
    TANGLE_ICE = "TANGLE_ICE",
    WORD_SEARCH_ICE = "WORD_SEARCH_ICE",
    NETWALK_ICE = "NETWALK_ICE",
    TAR_ICE = "TAR_ICE",
    SWEEPER_ICE = "SWEEPER_ICE",
}

export const OS = "OS"
export const TEXT = "TEXT"
export const TRACER = "TRACER"
export const TRACE_LOG = "TRACE_LOG"
export const SCAN_BLOCK = "SCAN_BLOCK"
export const MONEY = "MONEY"
export const CODE = "CODE"
export const CORE = "CORE"

export const PASSWORD_ICE = "PASSWORD_ICE"
export const TANGLE_ICE = "TANGLE_ICE"
export const WORD_SEARCH_ICE = "WORD_SEARCH_ICE"
export const NETWALK_ICE = "NETWALK_ICE"
export const TAR_ICE = "TAR_ICE"
export const SWEEPER_ICE = "SWEEPER_ICE"

export const LOCK = "LOCK"
export const STATUS_LIGHT = "STATUS_LIGHT"

export const KEYSTORE = "KEYSTORE"
export const TRIPWIRE = "TRIPWIRE"
export const TIMER_ADJUSTER = "TIMER_ADJUSTER"

export const SCRIPT_INTERACTION = LayerType.SCRIPT_INTERACTION.toString()
export const SCRIPT_CREDITS = LayerType.SCRIPT_CREDITS.toString()


export const layerTypeFromIceId = (iceId: string): LayerType | null => {
    const [iceType] = iceId.split("-")
    switch(iceType) {
        case "password": return LayerType.PASSWORD_ICE
        case "tangle": return LayerType.TANGLE_ICE
        case "wordSearch": return LayerType.WORD_SEARCH_ICE
        case "netwalk": return LayerType.NETWALK_ICE
        case "tar": return LayerType.TAR_ICE
        case "sweeper": return LayerType.SWEEPER_ICE
        default: return null
    }
}
export const iceThemeName = {
    PASSWORD_ICE: "Rahasy",
    TANGLE_ICE: "Gaanth",
    NETWALK_ICE: "Sanrachana",
    WORD_SEARCH_ICE: "Jaal",
    TAR_ICE: "Tar",
    SWEEPER_ICE: "Visphotak"
}

export const iceSimpleName = {
    PASSWORD_ICE: "Password",
    TANGLE_ICE: "Tangle",
    NETWALK_ICE: "Netwalk",
    WORD_SEARCH_ICE: "Word search",
    TAR_ICE: "Tar",
    SWEEPER_ICE: "Minesweeper"
}

export const iceDefaultOrder = {
    WORD_SEARCH_ICE: 1,
    TANGLE_ICE: 2,
    NETWALK_ICE: 3,
    SWEEPER_ICE: 4,
    TAR_ICE: 5,
    PASSWORD_ICE: 6,
}

export function iceTypeDefaultSorter(a: IceType, b: IceType) {
    return iceDefaultOrder[a] - iceDefaultOrder[b]
}
