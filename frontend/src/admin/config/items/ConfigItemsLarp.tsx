import React from "react"
import {ConfigItemText} from "../ConfigHome";
import {ConfigItem} from "../ConfigReducer";

export const ConfigItemLarpName = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Generic: Larp name" value={props.value} item={ConfigItem.LARP_NAME}/>
            <div className="form-text text-muted text-size">The name of the larp<br/><br/>
                Default: unknown<br/>
                General advice: enter your larp name<br/><br/>
                This is used in the file name of exports of sites.<br/>
            </div>
        </>
    )
}

export const ConfigItemLarpFrontierLolaEnabled = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Frontier: LOLA enabled" value={props.value} item={ConfigItem.LARP_SPECIFIC_FRONTIER_LOLA_ENABLED}/>
            <div className="form-text text-muted text-size">LOLA enabled<br/><br/>
                Default: false<br/>
                General advice: leave false<br/><br/>
                LOLA is an external system used at Frontier Larp. It connects to AttackVector as a hacker.<br/><br/>
            </div>
        </>
    )
}

export const ConfigItemLarpFrontierOrthankToken = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="Frontier: Orthank token" value={props.value} item={ConfigItem.LARP_SPECIFIC_FRONTIER_ORTHANK_TOKEN}/>
            <div className="form-text text-muted text-size">Orthank token<br/><br/>
                Default: (empty)<br/>
                General advice: leave empty<br/><br/>
                Orthank is a system used at Frontier Larp. It stores character information. If you are not running Frontier, you can ignore this
                field.<br/><br/>
            </div>
        </>
    )
}

export const ConfigItemLarpOpenIdConnectUrl = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="OpenId Connect: Url" value={props.value} item={ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL}/>
            <small className="form-text text-muted">OpenId Connect: Url<br/><br/>
                Default: (empty)<br/>
                General advice: leave empty<br/><br/>
                If you are not running with a custom OpenId Connect SSO, you can ignore this field.<br/>
                Ex with a local Keycloak: 'http://auth.localhost/realms/larp/protocol/openid-connect'
                <br/><br/>
            </small>
        </>
    )
}

export const ConfigItemLarpOpenIdConnectClientId = (props: { value: string }) => {
    return (
        <>
            <ConfigItemText name="OpenId Connect: Client Id" value={props.value} item={ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID}/>
            <small className="form-text text-muted">OpenId Connect: Client Id<br/><br/>
                Default: attack_vector<br/>
                General advice: leave default<br/><br/>
                Id representing attack_vector in your OpenId Connect SSO<br/>
                If you are not running with a custom OpenId Connect SSO, you can ignore this field.
                <br/><br/>
            </small>
        </>
    )
}
