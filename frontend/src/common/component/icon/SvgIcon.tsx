import React from "react"
import {LOCK, STATUS_LIGHT} from "../../enums/LayerTypes";

/* eslint jsx-a11y/alt-text: 0*/

interface Props {
    type?: string,
    color?: string
}

export const SvgIcon = (props: Props) => {
    const path = determinePath(props.type)
    const className = props.color ? "svg-color-" + props.color : ""
    return (<img src={path} className={className} width="22px" />)
}

const determinePath = (type: string| undefined) => {
    switch(type) {
        case LOCK: return "/img/editor/tabler/9081099_square_lock_lock_square.svg"
        case STATUS_LIGHT: return "/img/editor/tabler/9080499_bulb.svg"
        default: return "unknown-type:" + type
    }
}
