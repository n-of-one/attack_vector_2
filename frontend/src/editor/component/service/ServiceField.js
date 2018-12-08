import React from 'react';
import {connect} from "react-redux";

const mapDispatchToProps = (dispatch) => {
    return {
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({name, type, readOnly, value}) => {
        if (!readOnly) {
            readOnly = false;
        }

        let colSize = (type === "large") ? "col-lg-8 noRightPadding" : "col-lg-4 noRightPadding";
        return (
            <div className="row form-group serviceFieldRow">
                <div className="col-lg-3 serviceLabel">{name}</div>
                <div className={colSize}>
                    <span><input type="text" className="form-control input-sm serviceInputLong" disabled={readOnly} value={value}/></span>
                </div>
                <div className="col-lg-1 serviceHelpColumn">
                    <span className="badge helpBadge" >?</span>
                </div>
            </div>
        );
    });
