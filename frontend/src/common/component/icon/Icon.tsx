import React from "react"
import {Glyphicon} from "./Glyphicon";
import {SvgIcon} from "./SvgIcon";
import {
    CODE,
    CORE, LOCK,
    LINK,
    MONEY,
    NETWALK_ICE,
    OS,
    PASSWORD_ICE,
    SCAN_BLOCK,
    TAR_ICE, STATUS_LIGHT,
    TANGLE_ICE,
    TEXT,
    TIMER_TRIGGER,
    TRACE_LOG,
    TRACER,
    WORD_SEARCH_ICE, KEYSTORE
} from "../../enums/LayerTypes";

interface Props {
    type: string
    display?: string
    color?: string // use this if you want to always set the color
    svgColor?: string // use this if you only want to set the color if the icon is an svg. Use when glyphicon is colored by text
}

export const Icon = ({type, display, color, svgColor}: Props) => {
    const family = iconFamily(type!)


    switch (family) {
        case "glyphicon" :
            return <Glyphicon type={type} display={display} color={color}/>
        case "svg":
            const finalColor = svgColor ? svgColor : color
            return <SvgIcon type={type} color={finalColor}/>
        default:
            return <span className="text">Unknown icon family: {family}</span>
    }
}

const iconFamily = (type: string) => {
    switch (type) {
        case OS:
        case TEXT:
        case LINK:
        case TRACER:
        case TRACE_LOG:
        case SCAN_BLOCK:
        case MONEY:
        case CODE:
        case TIMER_TRIGGER:
        case CORE:
        case PASSWORD_ICE:
        case TANGLE_ICE:
        case TAR_ICE:
        case NETWALK_ICE:
        case WORD_SEARCH_ICE:
        case KEYSTORE:
            return "glyphicon"
        case LOCK:
        case STATUS_LIGHT:
            return "svg"
    }
}