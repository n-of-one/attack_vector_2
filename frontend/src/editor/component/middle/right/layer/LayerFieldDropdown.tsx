import React from 'react'
import {OverlayTrigger, Tooltip} from "react-bootstrap";

export interface DropdownOption {
    value: string,
    text: string
}

interface Props {
    label: string,
    value: string,
    options: DropdownOption[]
    save:(newValue: string) => void,
    tooltipText: string,
    tooltipId: string,
}
export const LayerFieldDropdown = ({label, value, save, options, tooltipId, tooltipText}: Props) => {

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{label}</div>
            <div className="col-lg-5 noRightPadding">
                <select className="form-control" onChange={(event) => save(event.target.value)} value={value}>
                    {options.map((option: DropdownOption) => <option value={option.value} key={option.value}>{option.text}</option>) }
                </select>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key={tooltipId}
                    placement="left"
                    overlay={
                        <Tooltip id={"tooltipId"}>{tooltipText}</Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>
        </div>
    )
}

