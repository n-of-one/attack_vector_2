import React from "react";
import {IceStrength} from "../../../common/model/IceStrength";


interface Props {
    name: string,
    strength: IceStrength
}

const strengthDescription = (strength: IceStrength) => {
    switch (strength) {
        case IceStrength.VERY_WEAK :
            return "Very weak"
        case IceStrength.WEAK:
            return "Weak"
        case IceStrength.AVERAGE:
            return "Average"
        case IceStrength.STRONG:
            return "Strong"
        case IceStrength.VERY_STRONG:
            return "Very strong"
        case IceStrength.ONYX:
            return "Onyx"
        default:
            return ""
    }

}

export const IceTitle = ({name, strength}: Props) => {
    return <h4 className="text-success d-flex justify-content-between">
        <strong>
            Ice: <span className="text-info">{name}</span>&nbsp;<br/>
            Strength: <span className="text-info">{strengthDescription(strength)}</span><br/>
        </strong>
    </h4>
}