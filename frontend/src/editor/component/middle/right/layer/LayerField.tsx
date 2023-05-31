import React from 'react'
import {TextSaveInput} from "../../../../../common/component/TextSaveInput"
import {OverlayTrigger, Tooltip} from "react-bootstrap"


const renderInput = (value: string | number, readOnly: boolean, save?: (value: string) => void, placeholder?: string) => {
    if (readOnly) {
        return (
            <span><input type="text" className="form-control input-sm" disabled={readOnly} value={value}/></span>
        )
    }
    return (
        <TextSaveInput className="form-control input-sm" placeholder={placeholder} value={value}
                       save={value => save!(value)}/>

    )
}

interface Props {
    label: string,
    size: string,
    readOnly?: boolean,
    value: string | number,
    save?: (value: string) => void,
    placeholder?: string,
    help: string
}

export const LayerField = ({label, size, readOnly, value, save, placeholder, help} : Props) => {
    if (!readOnly) {
        readOnly = false
    }

    let colSize = (size === "large") ? "col-lg-8 noRightPadding" : "col-lg-5 noRightPadding"
    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{label}</div>
            <div className={colSize}>
                {renderInput(value, readOnly, save, placeholder)}
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
