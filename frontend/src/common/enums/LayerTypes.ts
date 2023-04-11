export const OS = "OS";
export const TEXT = "TEXT";
export const PICTURE = "PICTURE";
export const LINK = "LINK";
export const TRACER = "TRACER";
export const TRACE_LOG = "TRACE_LOG";
export const SCAN_BLOCK = "SCAN_BLOCK";
export const MONEY = "MONEY";
export const CODE = "CODE";
export const TIMER_TRIGGER = "TIMER_TRIGGER";
export const CORE = "CORE";
export const PASSWORD_ICE = "PASSWORD_ICE";
export const ICE_FILM = "ICE_FILM";
export const NETWALK_ICE = "NETWALK_ICE";
export const WORD_SEARCH_ICE = "WORD_SEARCH_ICE";
export const MAGIC_EYE_ICE = "MAGIC_EYE_ICE";
export const ALTERNATE_ICE = "ALTERNATE_ICE";
export const UNHACKABLE_ICE = "UNHACKABLE_ICE";
export const TANGLE_ICE = "TANGLE_ICE";

export enum LayerType {
    OS = "OS",
    TEXT= "TEXT",
    TIMER_TRIGGER = "TIMER_TRIGGER",
    PASSWORD_ICE = "PASSWORD_ICE",
    TANGLE_ICE = "TANGLE_ICE",
    WORD_SEARCH_ICE = "WORD_SEARCH_ICE",
}



export const glyphiconFromType = (type: string) => {
    switch(type) {
        case OS : return "glyphicon-home";
        case TEXT : return "glyphicon-file";
        case PICTURE : return "glyphicon-picture";
        case LINK : return "glyphicon-link";
        case TRACER : return "glyphicon-transfer";
        case TRACE_LOG : return "glyphicon-erase";
        case SCAN_BLOCK : return "glyphicon-magnet";
        case MONEY : return "glyphicon-usd";
        case CODE : return "glyphicon-ok-circle";
        case TIMER_TRIGGER : return "glyphicon-time";
        case CORE : return "glyphicon-th-large";
        case PASSWORD_ICE : return "glyphicon-console";
        case TANGLE_ICE: return "glyphicon-asterisk";
        case ICE_FILM : return "glyphicon-film";
        case NETWALK_ICE : return "glyphicon-qrcode";
        case WORD_SEARCH_ICE : return "glyphicon-th";
        case MAGIC_EYE_ICE : return "glyphicon-eye-close";
        case ALTERNATE_ICE : return "glyphicon-star";
        case UNHACKABLE_ICE : return "glyphicon-ban-circle";
        default:
            console.log("unknown type:" + type);
            return "glyphicon-thumbs-down";
    }
};
