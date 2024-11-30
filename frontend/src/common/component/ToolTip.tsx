import {OverlayTrigger, Tooltip} from "react-bootstrap";
import React, {useState} from "react";
import {Placement} from "react-bootstrap/types";

interface Props {
    id: string,
    children: React.JSX.Element,
    text: string,
    placement?: Placement
}

export const ToolTip = (props: Props) => {
    const placement = props.placement || "left"
    return <OverlayTrigger
        key={props.id}
        placement={placement}
        overlay={
            <Tooltip id={props.id}>{props.text}</Tooltip>
        }
    >
        {props.children}
    </OverlayTrigger>
}

export const InfoBadge = (props: { infoText: string, placement?: Placement, badgeText?: string }) => {
    const badgeText = props.badgeText || "?"
    const [id] = useState(new Date().getTime() + ":" + Math.random())
    return <ToolTip id={id} text={props.infoText} placement={props.placement}>
        <span className="badge bg-secondary helpBadge">{badgeText}</span>
    </ToolTip>

}
