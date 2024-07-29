import React from 'react'
import {TextSaveInput, TextSaveType} from "../../../../../../common/component/TextSaveInput"
import {OverlayTrigger, Tooltip} from "react-bootstrap"


const renderInput = (value: string | number, readOnly: boolean, id?: string, type?: TextSaveType, save?: (value: string) => void, placeholder?: string, terminalPrefix?: string) => {
    if (readOnly) {
        return (
            <span><input type="text" className="form-control input-sm" disabled={readOnly} value={value}/></span>
        )
    }
    return (
        <TextSaveInput className="form-control input-sm" placeholder={placeholder} value={value} id={id} type={type}
                       save={value => save!(value)} terminalPrefix={terminalPrefix}/>
    )
}

interface Props {
    label: string,
    size: string,
    readOnly?: boolean,
    value: string | number,
    save?: (value: string) => void,
    placeholder?: string,
    help: string,
    id?: string,
    type?: TextSaveType
    terminalPrefix?: string
}

export const TextAttribute = ({label, size, readOnly, value, type, save, placeholder, help, id, terminalPrefix}: Props) => {
    if (!readOnly) {
        readOnly = false
    }

    let colSize = (size === "large") ? "col-lg-8 noRightPadding" : "col-lg-5 noRightPadding"
    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{label}</div>
            <div className={colSize}>
                {renderInput(value, readOnly, id, type, save, placeholder, terminalPrefix)}
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key={"tooltip_" + label}
                    placement="left"
                    overlay={
                        <Tooltip id={`tooltip-${label}`}>
                            {help}
                        </Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>
        </div>
    )
}
