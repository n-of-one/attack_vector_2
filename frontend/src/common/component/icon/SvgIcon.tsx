import React from "react"
import {CORE, LOCK, SCRIPT_CREDITS, STATUS_LIGHT, TRIPWIRE} from "../../enums/LayerTypes";
import { ReactComponent as CopyIcon } from "./svgIcons/9080754_copy.svg";
import { ReactComponent as PasteIcon } from "./svgIcons/9080712_plus_clipboard.svg";

export const COPY_ICON = "COPY_ICON"
export const PASTE_ICON = "PASTE_ICON"

interface Props {
    type?: string,
    color?: string
}

export const SvgIcon = (props: Props) => {
    if (props.type === COPY_ICON) {
        return <CopyIcon width="18px" height="18px" />
    }
    if (props.type === PASTE_ICON) {
        return <PasteIcon width="18px" height="18px" />
    }

    const path = determinePath(props.type)
    const className = props.color ? "svg-color-" + props.color : ""
    return (<img src={path} className={className} width="22px" />)
}

const determinePath = (type: string| undefined) => {
    switch(type) {
        case LOCK: return "/img/editor/tabler/9081099_square_lock_lock_square.svg"
        case STATUS_LIGHT: return "/img/editor/tabler/9080499_bulb.svg"
        case TRIPWIRE: return "/img/editor/tabler/9080987_history_history.svg"
        case CORE: return "/img/editor/tabler/9080715_cpu.svg"
        case SCRIPT_CREDITS:
            return "/img/editor/tabler/9080253_bolt.svg"
        default: return "unknown-type svg:" + type
    }
}
