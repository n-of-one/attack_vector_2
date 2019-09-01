const OS = "OS";
const TEXT = "TEXT";
const PICTURE = "PICTURE";
const LINK = "LINK";
const TRACER = "TRACER";
const TRACE_LOG = "TRACE_LOG";
const SCAN_BLOCK = "SCAN_BLOCK";
const MONEY = "MONEY";
const CODE = "CODE";
const TIME = "TIME";
const CORE = "CORE";
const ICE_PASSWORD = "ICE_PASSWORD";
const ICE_FILM = "ICE_FILM";
const ICE_NETWALK = "ICE_NETWALK";
const ICE_WORD_SEARCH = "ICE_WORD_SEARCH";
const ICE_MAGIC_EYE = "ICE_MAGIC_EYE";
const ICE_PASSWORD_SEARCH = "ICE_PASSWORD_SEARCH";
const ICE_ALTERNATE = "ICE_ALTERNATE";
const ICE_UNHACKABLE = "ICE_UNHACKABLE";
const ICE_UNTANGLE = "ICE_UNTANGLE";

const glyphiconFromType = (type) => {
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


export {
    glyphiconFromType,
    OS, TEXT, PICTURE, LINK, TRACER, TRACE_LOG, SCAN_BLOCK, MONEY, CODE, TIME, CORE,
    ICE_PASSWORD, ICE_FILM, ICE_NETWALK, ICE_WORD_SEARCH, ICE_MAGIC_EYE, ICE_PASSWORD_SEARCH, ICE_ALTERNATE, ICE_UNHACKABLE,
};