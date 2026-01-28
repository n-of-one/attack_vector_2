import {ScriptEffectDisplay} from "../ScriptModel";
import {InfoBadge} from "../../component/ToolTip";
import React from "react";

interface Props {
    effects: ScriptEffectDisplay[]
}

export const ScriptEffects = ({effects}: Props) => {
    return (<> {
        effects.map((effectDisplay, index) => {
            return (<span key={index}>
                        <InfoBadge infoText={effectDisplay.description}
                                   badgeText={effectDisplay.label}/>
                &nbsp;</span>)
        })
    } </>)
}
