import {Script} from "./ScriptModel";
import {InfoBadge} from "../component/ToolTip";
import React from "react";

interface Props {
    script: Script
}

export const ScriptEffects = ({script}: Props) => {
    return (<> {
        script.effects.map((effectDisplay, index) => {
            return (<span key={index}>
                        <InfoBadge infoText={effectDisplay.description}
                                   badgeText={effectDisplay.label}/>
                &nbsp;</span>)
        })
    } </>)
}
