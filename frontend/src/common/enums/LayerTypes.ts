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

export const LOCK = "LOCK"
export const STATUS_LIGHT = "STATUS_LIGHT"

export const KEYSTORE = "KEYSTORE"
export const TRIPWIRE = "TRIPWIRE"

export enum LayerType {
    OS = "OS",
    TEXT= "TEXT",
    PASSWORD_ICE = "PASSWORD_ICE",
    TANGLE_ICE = "TANGLE_ICE",
    WORD_SEARCH_ICE = "WORD_SEARCH_ICE",
    NETWALK_ICE = "NETWALK_ICE",
    TAR_ICE = "TAR_ICE",
    LOCK = "LOCK",
    STATUS_LIGHT = "STATUS_LIGHT",
    KEYSTORE = "KEYSTORE",
    TRIPWIRE = "TRIPWIRE",
    CORE = "CORE",
}

export const layerTypeFromIceId = (iceId: string): LayerType | null => {
    const [iceType] = iceId.split("-")
    switch(iceType) {
        case "password": return LayerType.PASSWORD_ICE
        case "tangle": return LayerType.TANGLE_ICE
        case "wordSearch": return LayerType.WORD_SEARCH_ICE
        case "netwalk": return LayerType.NETWALK_ICE
        case "tar": return LayerType.TAR_ICE
        default: return null
    }
}
