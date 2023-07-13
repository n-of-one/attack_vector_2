import React from "react"
import {
    CODE,
    CORE,
    LINK,     MONEY, NETWALK_ICE,
    OS,
    PASSWORD_ICE,
    PICTURE,
    SCAN_BLOCK, TAR_ICE,
    TANGLE_ICE,
    TEXT,
    TIMER_TRIGGER,
    TRACE_LOG,
    TRACER, WORD_SEARCH_ICE
} from "../../enums/LayerTypes"


interface Props {
    type?: string,
    name?: string,
    display?: string,
    color?: string
}

export const Glyphicon = (props: Props) => {
    const className = determineClassName(props)
    const size = "18px"
    const height= "24px"
    const display = (props.display) ? props.display : "inherit"

    return (<span className={className} style={{"fontSize": size, display: display, zIndex: "100", color: props.color, height: height }}/>)
}

const determineClassName = ({type, name}: {type?: string, name?: string}) => {
    if (name) {
        return "glyphicon " + name
    }
    return "glyphicon " + glyphiconFromType(type!)
}


export const glyphiconFromType = (type: string) => {
    switch(type) {
        case OS : return "glyphicon-home"
        case TEXT : return "glyphicon-file"
        case PICTURE : return "glyphicon-picture"
        case LINK : return "glyphicon-link"
        case TRACER : return "glyphicon-transfer"
        case TRACE_LOG : return "glyphicon-erase"
        case SCAN_BLOCK : return "glyphicon-magnet"
        case MONEY : return "glyphicon-usd"
        case CODE : return "glyphicon-ok-circle"
        case TIMER_TRIGGER : return "glyphicon-time"
        case CORE : return "glyphicon-th-large"
        case PASSWORD_ICE : return "glyphicon-console"
        case TANGLE_ICE: return "glyphicon-asterisk"
        case TAR_ICE : return "glyphicon-hourglass"
        case NETWALK_ICE : return "glyphicon-qrcode"
        case WORD_SEARCH_ICE : return "glyphicon-th"
        default:
            console.log("unknown type:" + type)
            return "glyphicon-thumbs-down"
    }
}