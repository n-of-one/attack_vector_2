import React from 'react';
import TextSaveInput from "../../../../../common/component/TextSaveInput";
import {OverlayTrigger, Tooltip} from "react-bootstrap";


const renderInput = (value, readOnly, save, placeholder) => {
    if (readOnly) {
        return (
            <span><input type="text" className="form-control input-sm" disabled={readOnly} value={value}/></span>
        );
    }
    return (
        <TextSaveInput className="form-control input-sm" placeholder={placeholder} value={value}
                       save={value => save(value)}/>

    );
};

export default ({name, size, readOnly, value, save, placeholder, help}) => {
    if (!readOnly) {
        readOnly = false;
    }

    let colSize = (size === "large") ? "col-lg-8 noRightPadding" : "col-lg-5 noRightPadding";
    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{name}</div>
            <div className={colSize}>
                {renderInput(value, readOnly, save, placeholder)}
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key={"tooltip_" + name}
                    placement="left"
                    overlay={
                        <Tooltip id={`tooltip-${name}`}>
                            {help}
                        </Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
        </div>
</div>
)
    ;
};
