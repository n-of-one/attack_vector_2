import React from "react"
import {
    CODE,
    MONEY, NETWALK_ICE,
    OS,
    PASSWORD_ICE,
    SCAN_BLOCK, TAR_ICE,
    TANGLE_ICE,
    TEXT,
    TRACE_LOG,
    TRACER, WORD_SEARCH_ICE, KEYSTORE, SWEEPER_ICE
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
        case KEYSTORE : return "glyphicon-briefcase"
        case TRACER : return "glyphicon-transfer"
        case TRACE_LOG : return "glyphicon-erase"
        case SCAN_BLOCK : return "glyphicon-magnet"
        case MONEY : return "glyphicon-usd"
        case CODE : return "glyphicon-ok-circle"
        case PASSWORD_ICE : return "glyphicon-console"
        case TANGLE_ICE: return "glyphicon-asterisk"
        case TAR_ICE : return "glyphicon-hourglass"
        case NETWALK_ICE : return "glyphicon-qrcode"
        case WORD_SEARCH_ICE : return "glyphicon-th"
        case SWEEPER_ICE: return "glyphicon-flag"
        default:
            console.log("unknown type:" + type)
            return "glyphicon-thumbs-down"
    }
}