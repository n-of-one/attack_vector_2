import React from 'react'
import {IceStrength} from "../../../../../common/model/IceStrength";
import {OverlayTrigger, Tooltip} from "react-bootstrap";

interface Props {
    value: string,
    save:(newValue: string) => void
}
export const LayerStrength = ({value, save}: Props) => {

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">Strength</div>
            <div className="col-lg-5 noRightPadding">
                <select className="form-control" onChange={(event) => save(event.target.value)} value={value}>
                    <option value={IceStrength.VERY_WEAK}>Very weak</option>
                    <option value={IceStrength.WEAK}>Weak</option>
                    <option value={IceStrength.AVERAGE}>Average</option>
                    <option value={IceStrength.STRONG}>Strong</option>
                    <option value={IceStrength.VERY_STRONG}>Very Strong</option>
                    <option value={IceStrength.ONYX}>Onyx (Extreme)</option>
                </select>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key={"tooltip_ice_strength"}
                    placement="left"
                    overlay={
                        <Tooltip id={"tooltip_ice_strength"}>Stronger Ice takes longer to hack or require scripts to overcome.</Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>
        </div>
    )
}

