import React from "react"
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemLarpName = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Generic: Larp name" value={props.value} item={ConfigItem.LARP_NAME}/>
            <small className="form-text text-muted">The name of the larp<br/><br/>
                Default: unknown<br/>
                General advice: enter your larp name<br/><br/>
                This is used in the file name of exports of sites.<br/>
            </small>
        </>
    )
}

export const ConfigItemLarpFrontierLolaEnabled = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Frontier: LOLA enabled" value={props.value} item={ConfigItem.LARP_SPECIFIC_FRONTIER_LOLA_ENABLED}/>
            <small className="form-text text-muted">LOLA enabled<br/><br/>
                Default: false<br/>
                General advice: leave false<br/><br/>
                LOLA is an external system used at Frontier Larp. It connects to AttackVector as a hacker.<br/><br/>
            </small>
        </>
    )
}

export const ConfigItemLarpFrontierOrthankToken = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Frontier: Orthank token" value={props.value} item={ConfigItem.LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN}/>
            <small className="form-text text-muted">Orthank token<br/><br/>
                Default: (empty)<br/>
                General advice: leave empty<br/><br/>
                Orthank is a system used at Frontier Larp. It stores character information. If you are not running Frontier, you can ignore this
                field.<br/><br/>
            </small>
        </>
    )
}
