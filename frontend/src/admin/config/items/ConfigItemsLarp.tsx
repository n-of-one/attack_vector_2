import React from "react"
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemLarpName = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="LARP: name" value={props.value} item={ConfigItem.LARP_NAME}/>
            <small className="form-text text-muted">The name of the larp<br/><br/>
                Default: unknown<br/><br/>
                General advice: enter your larp name<br/>
                This is used in two areas:<br/>
                <ul><br/>
                    <li>The file name of exports of the sites. Having a distinct name might be useful if you manage multiple larps.<br/><br/></li>
                    <li>Some LARP specific features are triggered by the larp name. This is currently only triggered for "frontier", so don't use that name
                        unless you are running this larp.
                    </li>
                </ul>
            </small>
        </>
    )
}

export const ConfigItemLarpFrontierOrthankToken = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Frontier: Orthank token" value={props.value} item={ConfigItem.FRONTIER_ORTHANK_TOKEN}/>
            <small className="form-text text-muted">Frontier specific configuration<br/><br/>
                Default: (empty)<br/><br/>
                General advice: leave empty<br/><br/>
                Orthank is a system used at Frontier Larp. It stores character information. If you are not running Frontier, you can ignore this
                field.<br/><br/>
            </small>
        </>
    )
}
