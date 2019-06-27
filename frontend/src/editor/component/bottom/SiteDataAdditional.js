import React from 'react';
import {connect} from "react-redux";
import {EDIT_SITE_DATA} from "../../EditorActions";
import TextSaveInput from "../../../common/component/TextSaveInput";

const mapDispatchToProps = (dispatch) => {
    return {
        save: (field, value) => { dispatch({type: EDIT_SITE_DATA, field: field, value: value}); },
    }
};
let mapStateToProps = (state) => {
    return {
        siteData: state.siteData,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({siteData, save}) => {
        return (
            <span>
            <div className="row">
                <div className="col-lg-6">
                    <div className="row form-horizontal" >
                        <div className="form-group has-feedback">
                            <label htmlFor="site_description" className="col-lg-3 control-label text-muted">Description</label>
                            <div className="col-lg-8">
                                <TextSaveInput type="textArea" id="site_description" rows="2" className="form-control" placeholder="For GM only" value={siteData.description} save={ (value) => save("description", value) } />
                            </div>
                        </div>
                    </div>
                </div>
                <div className="col-lg-6">
                    <div className="row form-horizontal" >
                        <div className="form-group has-feedback">
                            <label htmlFor="gm_name" className="col-lg-3 control-label text-muted">Creator</label>
                            <div className="col-lg-8">
                                <TextSaveInput id="gm_name" className="form-control" placeholder="" value={siteData.creator} save={ (value) => save("creator", value) }/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
    </span>
        );
    });
