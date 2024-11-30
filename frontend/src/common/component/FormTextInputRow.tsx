import {TextSaveInput} from "./TextSaveInput";
import React, {useState} from "react";


interface Props {
    label: string,
    value: string,
    labelColumns?: number,
    valueColumns?: number,
    save: (value: string) => void,
    placeHolder?: string,
}

export const FormTextInputRow = (props: Props) => {
    const [id] = useState(new Date().getTime() + ":" + Math.random())

    const labelColumns = props.labelColumns ? props.labelColumns : 2
    const valueColumns = props.valueColumns ? props.valueColumns : 4

    return (
        <div className="row form-group text">
            <div className="colg-lg-2"/>
            <label htmlFor={id} className={`col-lg-${labelColumns} control-label text-muted`}>{props.label}</label>
            <div className={`col-lg-${valueColumns}`}>
                <TextSaveInput id={id} className="form-control"
                               placeholder="" value={props.value}
                               save={props.save}/>

            </div>
        </div>

    )
}
