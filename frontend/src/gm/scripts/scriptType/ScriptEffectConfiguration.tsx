import {Effect, EffectType, ScriptType} from "../../../common/script/type/ScriptTypeReducer";
import {TextSaveInput, TextSaveType} from "../../../common/component/TextSaveInput";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {DropDownSaveInput} from "../../../common/component/DropDownSaveInput";
import {iceSimpleName, IceType, iceTypeDefaultSorter} from "../../../common/enums/LayerTypes";
import React from "react";
import {InfoBadge} from "../../../common/component/ToolTip";
import {SilentLink} from "../../../common/component/SilentLink";
import {HackIceByStrengthRows} from "./ScriptTypeManagementHackIceByStrength";
import {avEncodedPath} from "../../../common/util/PathEncodeUtils";

/* eslint react/jsx-no-target-blank  : 0*/

export const EffectConfigurationLine = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    switch (effect.type) {
        case EffectType.AUTO_HACK_ANY_ICE:
        case EffectType.HACK_BELOW_NON_HACKED_ICE:
        case EffectType.HIDDEN_EFFECTS:
        case EffectType.SCAN_ICE_NODE:
        case EffectType.SWEEPER_UNBLOCK:
        case EffectType.ROTATE_ICE:
        case EffectType.SITE_STATS:
        case EffectType.TANGLE_REVEAL_CLUSTERS:
            return <EffectRowZeroColumns effect={effect} scriptType={scriptType}/>

        case EffectType.JUMP_TO_HACKER:
        case EffectType.JUMP_TO_NODE:
            return <EffectRowOneColumn effect={effect} scriptType={scriptType}>
                <EffectValueJumpBlockedType effect={effect} scriptType={scriptType}/>
            </EffectRowOneColumn>

        case EffectType.AUTO_HACK_SPECIFIC_ICE_LAYER:
        case EffectType.DELAY_TRIPWIRE_COUNTDOWN:
        case EffectType.DECREASE_FUTURE_TIMERS:
        case EffectType.INTERACT_WITH_SCRIPT_LAYER:
        case EffectType.SPEED_UP_RESET_TIMER:
        case EffectType.START_RESET_TIMER:
        case EffectType.WORD_SEARCH_NEXT_WORDS:
            return <EffectRowOneColumn effect={effect} scriptType={scriptType}>
                <EffectValueText effect={effect} scriptType={scriptType}/>
            </EffectRowOneColumn>

        case EffectType.AUTO_HACK_ICE_TYPE:
            return <EffectRowOneColumn effect={effect} scriptType={scriptType}>
                <EffectValueIceType effect={effect} scriptType={scriptType}/>
            </EffectRowOneColumn>


        case EffectType.SHOW_MESSAGE:
            return <ShowMessageEffectRow effect={effect} scriptType={scriptType}/>

        case EffectType.AUTO_HACK_ICE_BY_STRENGTH:
            return <EffectRowMultipleRows effect={effect} scriptType={scriptType}>
                <HackIceByStrengthRows effect={effect} scriptType={scriptType}/>
            </EffectRowMultipleRows>

        default:
            return <div className="col-lg-12 text">Missing effect type, see: ScriptTypeManagement.tsx</div>
    }
}

const EffectRowZeroColumns = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <EffectRowOneColumn effect={effect} scriptType={scriptType}>
        <div className="col-lg-4"/>
    </EffectRowOneColumn>
}


const EffectRowOneColumn = ({scriptType, effect, children}: { scriptType: ScriptType, effect: Effect, children: React.JSX.Element, }) => {
    const deleteEffect = () => {
        webSocketConnection.send("/gm/scriptType/deleteEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber})
    }

    return <>
        <div className="row text">
            <div className="col-lg-11">
                <hr/>
            </div>
        </div>
        <div className="row form-group text" data-row="script-effect-one-row">
            <div className="col-lg-1" style={{marginTop: "0", marginRight: "-30px"}}>
                <InfoBadge infoText={effect.gmDescription} placement="top"/>
            </div>

            <div className={`col-lg-6`}>
                {effect.name}
            </div>

            {children}

            <div className="col-lg-1" style={{paddingTop: "9px"}}>
                <SilentLink onClick={deleteEffect} title="Remove effect">
                    <span className="glyphicon glyphicon-trash"/>
                </SilentLink>
            </div>
        </div>
    </>
}

const EffectRowMultipleRows = ({scriptType, effect, children}: { scriptType: ScriptType, effect: Effect, children: React.JSX.Element, }) => {
    const deleteEffect = () => {
        webSocketConnection.send("/gm/scriptType/deleteEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber})
    }

    return <>
        <div className="row text">
            <div className="col-lg-11">
                <hr/>
            </div>
        </div>
        <div className="row form-group text">
            <div className="col-lg-1" style={{marginTop: "0", marginRight: "-30px"}}>
                <InfoBadge infoText={effect.gmDescription} placement="top"/>
            </div>
            <div className={"col-lg-10"}>
                {effect.name}
            </div>
            <div className="col-lg-1" style={{paddingTop: "9px"}}>
                <SilentLink onClick={deleteEffect} title="Remove effect">
                    <span className="glyphicon glyphicon-trash"/>
                </SilentLink>
            </div>
        </div>
        {children}
    </>
}


const ShowMessageEffectRow = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    const deleteEffect = () => {
        webSocketConnection.send("/gm/scriptType/deleteEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber})
    }

    return <>
        <div className="row text">
            <div className="col-lg-11">
                <hr/>
            </div>
        </div>
        <div className="row form-group text" data-row="script-effect-one-row">
            <div className="col-lg-1" style={{marginTop: "0", marginRight: "-30px"}}>
                <InfoBadge infoText={effect.gmDescription} placement="top"/>
            </div>

            <div className={`col-lg-6`}>
                {effect.name}
            </div>
            <div className="col-lg-4">&nbsp;</div>
            <div className="col-lg-1" style={{paddingTop: "9px"}}>
                <SilentLink onClick={deleteEffect} title="Remove effect">
                    <span className="glyphicon glyphicon-trash"/>
                </SilentLink>
            </div>
        </div>
        <EffectValueTerminalText effect={effect} scriptType={scriptType}/>

    </>
}


export const EffectValueText = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <div className="col-lg-4">
        <TextSaveInput className="form-control"
                       placeholder="" value={effect.value as string}
                       save={(value) => {
                           webSocketConnection.send("/gm/scriptType/editEffect", {"scriptTypeId": scriptType.id, "effectNumber": effect.effectNumber, value})
                       }}
                       readonly={false}/>
    </div>
}


export const EffectValueJumpBlockedType = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <div className="col-lg-4">
        <DropDownSaveInput className="form-control"
                           selectedValue={effect.value as string}
                           save={(value) => {
                               webSocketConnection.send("/gm/scriptType/editEffect", {
                                   "scriptTypeId": scriptType.id,
                                   "effectNumber": effect.effectNumber,
                                   value
                               })
                           }}
                           readonly={false}>
            <>
                <option value="IGNORE_ICE">Ignoring ICE</option>
                <option value="BLOCKED_BY_ICE">Blocked by ICE</option>
            </>
        </DropDownSaveInput>
    </div>
}

export const EffectValueIceType = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    const options = Object.values(IceType)
        .sort(iceTypeDefaultSorter)
        .map((iceType =>
                <option value={iceType}>{iceSimpleName[iceType]}</option>
        ))

    return <div className="col-lg-4">
        <DropDownSaveInput className="form-control"
                           selectedValue={effect.value as string}
                           save={(value) => {
                               webSocketConnection.send("/gm/scriptType/editEffect", {
                                   "scriptTypeId": scriptType.id,
                                   "effectNumber": effect.effectNumber,
                                   value
                               })
                           }}
                           readonly={false}>
            <>{options}</>
        </DropDownSaveInput>
    </div>
}

export const EffectValueTerminalText = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {

    const encodedPath = avEncodedPath(`EFFECT|${scriptType.id}|${effect.effectNumber}|${scriptType.name} effect ${effect.effectNumber}`)

    return <>
        <div className="row">
            <div className="col-lg-10 text">
                <TextSaveInput className="form-control"
                               placeholder="Text to be shown to hackers."
                               value={effect.value as string}
                               save={(value) => {
                                   webSocketConnection.send("/gm/scriptType/editEffect", {
                                       "scriptTypeId": scriptType.id,
                                       "effectNumber": effect.effectNumber,
                                       value
                                   })
                               }}
                               readonly={false} rows={3}
                               type={TextSaveType.TEXTAREA}
                />
            </div>
        </div>
        <div className="row">
            <div className="col-lg-10 text">
                <br/>
                <a href={`/editText/${encodedPath}`} target="_blank">(click to
                    edit with terminal preview)</a>
            </div>
        </div>


    </>
}
