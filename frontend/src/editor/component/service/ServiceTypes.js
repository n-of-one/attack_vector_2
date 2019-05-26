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
const PASSWORD = "PASSWORD";
const FILM = "FILM";
const NETWALK = "NETWALK";
const WORD_SEARCH = "WORD_SEARCH";
const MAGIC_EYE = "MAGIC_EYE";
const PASSWORD_SEARCH = "PASSWORD_SEARCH";
const ALTERNATE = "ALTERNATE";
const UNHACKABLE = "UNHACKABLE";


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
        case PASSWORD : return "glyphicon-console";
        case FILM : return "glyphicon-film";
        case NETWALK : return "glyphicon-qrcode";
        case WORD_SEARCH : return "glyphicon-th";
        case MAGIC_EYE : return "glyphicon-eye-close";
        case PASSWORD_SEARCH : return "glyphicon-tasks";
        case ALTERNATE : return "glyphicon-star";
        case UNHACKABLE : return "glyphicon-ban-circle";
        default:
            console.log("unknown type:" + type);
            return "glyphicon-thumbs-down";

    }
};


export {
    glyphiconFromType,
    OS, TEXT, PICTURE, LINK, TRACER, TRACE_LOG, SCAN_BLOCK, MONEY, CODE, TIME, CORE,
    PASSWORD, FILM, NETWALK, WORD_SEARCH, MAGIC_EYE, PASSWORD_SEARCH, ALTERNATE, UNHACKABLE,
};