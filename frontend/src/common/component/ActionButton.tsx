import React from "react";
import {SilentLink} from "./SilentLink";

interface Props {
    children?: React.JSX.Element,
    onClick: () => void
    text?: string
    buttonClassName?: string
}

export const ActionButton = (props: Props) => {
    const buttonClassName = "btn " + (props.buttonClassName ? props.buttonClassName : "btn-info")

    return (
        <SilentLink onClick={props.onClick}>
            <span className={buttonClassName} style={{fontSize: "12px"}}>
                {props.children}{props.text}
            </span>
        </SilentLink>
    )
}
