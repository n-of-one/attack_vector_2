import React from 'react';
import {connect} from "react-redux";
import TextSaveInput from "../../../../../common/component/TextSaveInput";

export default ({value, save}) => {

        return (
            <div className="row form-group layerFieldRow">
                <div className="col-lg-3 layerLabel">Strength</div>
                <div className="col-lg-5 noRightPadding">
                    <TextSaveInput className="form-control input-sm"
                                   placeholder="Ice strength"
                                   value={value}
                                   save={ value => save(value) } />
                </div>
                <div className="col-lg-1 layerHelpColumn">
                    <span className="badge helpBadge" title="very_weak - weak - average - strong - very_strong - impenetrable - unknown" >?</span>
                </div>
            </div>
        );
    };
