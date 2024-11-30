import React from "react";
import {SilentLink} from "./SilentLink";

interface Props {
    children?: React.JSX.Element,
    onClick: () => void
    text?: string
}

export const ActionButton = (props: Props) => {
    return (
        <SilentLink onClick={props.onClick}>
            <span className="btn btn-info" style={{fontSize: "12px"}}>
                {props.children}{props.text}
            </span>
        </SilentLink>
    )
}
