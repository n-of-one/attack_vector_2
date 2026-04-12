import React from "react"
import {Glyphicon} from "./Glyphicon";
import {COPY_ICON, PASTE_ICON, SvgIcon} from "./SvgIcon";
import {
    CODE,
    CORE,
    KEYSTORE,
    LOCK,
    MONEY,
    NETWALK_ICE,
    OS,
    PASSWORD_ICE,
    SCAN_BLOCK,
    SCRIPT_CREDITS,
    SCRIPT_INTERACTION,
    STATUS_LIGHT,
    SWEEPER_ICE,
    TANGLE_ICE,
    TAR_ICE,
    TEXT,
    TRACE_LOG,
    TRACER,
    TRIPWIRE,
    TIMER_ADJUSTER,
    WORD_SEARCH_ICE
} from "../../enums/LayerTypes";

interface Props {
    type: string
    display?: string
    color?: string // use this if you want to always set the color
    svgColor?: string // use this if you only want to set the color if the icon is an svg. Use when glyphicon is colored by text
    size?: string
    height?: string
}

export const Icon = ({type, display, color, svgColor, size, height}: Props) => {
    const family = iconFamily(type!)


    switch (family) {
        case "glyphicon" :
            return <Glyphicon type={type} display={display} color={color} size={size} height={height}/>
        case "svg":
            const finalColor = svgColor ? svgColor : color
            return <SvgIcon type={type} color={finalColor}/>
        default:
            return <span className="text">Unknown icon family: {family}</span>
    }
}

const iconFamily = (type: string) => {
    if (type.toLowerCase().startsWith("glyphicon")) {
        return "glyphicon"
    }

    switch (type) {
        case OS:
        case TEXT:
        case TRACER:
        case TRACE_LOG:
        case SCAN_BLOCK:
        case MONEY:
        case CODE:
        case PASSWORD_ICE:
        case TANGLE_ICE:
        case TAR_ICE:
        case NETWALK_ICE:
        case WORD_SEARCH_ICE:
        case KEYSTORE:
        case SWEEPER_ICE:
        case SCRIPT_INTERACTION:
        case TIMER_ADJUSTER:
            return "glyphicon"
        case LOCK:
        case TRIPWIRE:
        case STATUS_LIGHT:
        case CORE:
        case SCRIPT_CREDITS:
        case COPY_ICON:
        case PASTE_ICON:
            return "svg"
        default: throw new Error("Unknown icon family for type: " + type)
    }
}
