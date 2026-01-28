import {Effect, ScriptType} from "../../../common/script/type/ScriptTypeReducer";
import {IceStrength, iceStrengthValue} from "../../../common/model/IceStrength";
import {strengthDescription} from "../../../standalone/ice/common/IceTitle";
import {DropDownSaveInput} from "../../../common/component/DropDownSaveInput";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {iceSimpleName, IceType, iceTypeDefaultSorter} from "../../../common/enums/LayerTypes";
import React from "react";

export const HackIceByStrengthRows = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    return <>
        <IceStrengthRow effect={effect} scriptType={scriptType}/>
        <div className="row">&nbsp;</div>
        <ExcludeIceTypesRows scriptType={scriptType} effect={effect}/>
    </>

}

const IceStrengthRow = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {
    const strengthInput = effect.value?.split(":")[0] || "WEAK"
    const excludesIceTypes = effect.value?.split(":")[1] || "PASSWORD_ICE,TAR_ICE"

    const options = (Object.values(IceStrength) as IceStrength[])
        .sort((a, b) => iceStrengthValue[b] - iceStrengthValue[a])
        .map(strength => <option value={strength}>{strengthDescription(strength)}</option>)

    return <div className="row form-group text">
        <div className="col-lg-1">&nbsp;</div>
        <div className="col-lg-4">
            Max strength script can hack
        </div>

        <div className="col-lg-4">
            <DropDownSaveInput className="form-control"
                               selectedValue={strengthInput}
                               save={(value) => {
                                   const newValue = `${value}:${excludesIceTypes}`

                                   webSocketConnection.send("/gm/scriptType/editEffect", {
                                       "scriptTypeId": scriptType.id,
                                       "effectNumber": effect.effectNumber,
                                       "value": newValue,
                                   })
                               }}
                               readonly={false}>
                <>{options}</>
            </DropDownSaveInput>
        </div>
    </div>
}


export const ExcludeIceTypesRows = ({scriptType, effect}: { scriptType: ScriptType, effect: Effect }) => {

    const rows = (Object.values(IceType) as IceType[])
        .sort(iceTypeDefaultSorter)
        .filter((type: IceType) => type !== IceType.PASSWORD_ICE)
        .map((type: IceType) => <ExcludeIceTypeRow scriptType={scriptType} effect={effect} iceType={type}/>
        )

    return <>
        {rows}
        <div className="row form-group text">
            <div className="col-lg-1">&nbsp;</div>
            <div className="col-lg-4">
                Password ICE
            </div>
            <div className="col-lg-4">
                <select className="form-control">
                    <option value="true">Never affected</option>
                </select>
            </div>
        </div>
    </>
}

const ExcludeIceTypeRow = ({scriptType, effect, iceType}: { scriptType: ScriptType, effect: Effect, iceType: IceType }) => {
    const strengthInput = effect.value?.split(":")[0] || "WEAK"

    const enabled = effect.value?.includes(iceType) || false
    const excludedTypesString = effect.value?.split(":")[1] || "PASSWORD_ICE"
    const excludedTypes = excludedTypesString.split(",")
    const excludedTypeWithoutThisType = excludedTypes.filter((t) => t !== iceType)
    const excludedTypeWithoutThisTypeString = excludedTypeWithoutThisType.join(",")

    return <div className="row form-group text">
        <div className="col-lg-1">&nbsp;</div>
        <div className="col-lg-4">
            {iceSimpleName[iceType]} ICE
        </div>
        <div className="col-lg-4">
            <DropDownSaveInput className="form-control"
                               selectedValue={enabled.toString()}
                               save={(value: string) => {
                                   const newExcludeTarIce = (value === "true") ? `,${iceType}` : ""
                                   const newExcludeIceTypes = `${excludedTypeWithoutThisTypeString}${newExcludeTarIce}`
                                   const newValue = `${strengthInput}:${newExcludeIceTypes}`

                                   webSocketConnection.send("/gm/scriptType/editEffect", {
                                       "scriptTypeId": scriptType.id,
                                       "effectNumber": effect.effectNumber,
                                       "value": newValue,
                                   })
                               }}
                               readonly={false}>
                <>
                    <option value="true">Not affected</option>
                    <option value="false">Affected</option>
                </>
            </DropDownSaveInput>
        </div>
    </div>

}
