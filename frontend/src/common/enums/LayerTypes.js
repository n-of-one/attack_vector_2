export const OS = "OS";
export const TEXT = "TEXT";
export const PICTURE = "PICTURE";
export const LINK = "LINK";
export const TRACER = "TRACER";
export const TRACE_LOG = "TRACE_LOG";
export const SCAN_BLOCK = "SCAN_BLOCK";
export const MONEY = "MONEY";
export const CODE = "CODE";
export const TIME = "TIME";
export const CORE = "CORE";
export const ICE_PASSWORD = "ICE_PASSWORD";
export const ICE_FILM = "ICE_FILM";
export const ICE_NETWALK = "ICE_NETWALK";
export const ICE_WORD_SEARCH = "ICE_WORD_SEARCH";
export const ICE_MAGIC_EYE = "ICE_MAGIC_EYE";
export const ICE_PASSWORD_SEARCH = "ICE_PASSWORD_SEARCH";
export const ICE_ALTERNATE = "ICE_ALTERNATE";
export const ICE_UNHACKABLE = "ICE_UNHACKABLE";
export const ICE_TANGLE = "ICE_TANGLE";
export const glyphiconFromType = (type) => {
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
        case TIME : return "glyphicon-time";
        case CORE : return "glyphicon-th-large";
        case ICE_PASSWORD : return "glyphicon-console";
        case ICE_TANGLE: return "glyphicon-asterisk";
        case ICE_FILM : return "glyphicon-film";
        case ICE_NETWALK : return "glyphicon-qrcode";
        case ICE_WORD_SEARCH : return "glyphicon-th";
        case ICE_MAGIC_EYE : return "glyphicon-eye-close";
        case ICE_PASSWORD_SEARCH : return "glyphicon-tasks";
        case ICE_ALTERNATE : return "glyphicon-star";
        case ICE_UNHACKABLE : return "glyphicon-ban-circle";
        default:
            console.log("unknown type:" + type);
            return "glyphicon-thumbs-down";

    }
};
