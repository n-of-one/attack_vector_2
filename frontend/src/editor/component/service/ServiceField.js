import React from 'react';
import {connect} from "react-redux";
import TextSaveInput from "../../../common/component/TextSaveInput";

const mapDispatchToProps = (dispatch) => {
    return {
        fieldChange: (data) => alert("data=" + data)
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

const renderInput = (value, readOnly, save, placeholder) => {
    if(readOnly) {
        return (
            <span><input type="text" className="form-control input-sm serviceInputLong" disabled={readOnly} value={value} /></span>
        );
    }
    return (
        <TextSaveInput className="form-control input-sm" placeholder={placeholder} value={value} save={ value => save(value) } />

    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({name, size, readOnly, value, save, placeholder, help}) => {
        if (!readOnly) {
            readOnly = false;
        }

        let colSize = (size === "large") ? "col-lg-8 noRightPadding" : "col-lg-4 noRightPadding";
        return (
            <div className="row form-group serviceFieldRow">
                <div className="col-lg-3 serviceLabel">{name}</div>
                <div className={colSize}>
                    {renderInput(value, readOnly, save, placeholder)}
                </div>
                <div className="col-lg-1 serviceHelpColumn">
                    <span className="badge helpBadge" title={help}>?
                    </span>
                </div>
            </div>
        );
    });
