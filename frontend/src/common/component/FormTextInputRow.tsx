import {TextSaveInput} from "./TextSaveInput";
import React, {useState} from "react";
import {InfoBadge} from "./ToolTip";


interface Props {
    label: string,
    value: string,
    labelColumns?: number,
    valueColumns?: number,
    save: (value: string) => void,
    placeHolder?: string,
    toolTip?: string
    maxLength?: number
}

export const FormTextInputRow = (props: Props) => {
    const [id] = useState(new Date().getTime() + ":" + Math.random())

    const labelColumns = props.labelColumns ? props.labelColumns : 3
    const valueColumns = props.valueColumns ? props.valueColumns : 4

    const infoBadge = props.toolTip ? <InfoBadge infoText={props.toolTip} placement="bottom"></InfoBadge> : <></>


    return (
        <div className="row form-group text">
            <div className="colg-lg-2"/>
            <label htmlFor={id} className={`col-lg-${labelColumns} control-label text-muted`}>{infoBadge} {props.label}</label>
            <div className={`col-lg-${valueColumns}`}>
                <TextSaveInput id={id} className="form-control" maxLength={props.maxLength}
                               placeholder="" name={props.label} value={props.value}
                               save={props.save}/>

            </div>
        </div>

    )
}
