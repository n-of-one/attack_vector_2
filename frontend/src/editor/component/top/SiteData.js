import React from 'react';
import {connect} from "react-redux";
import {EDIT_SITE_DATA} from "../../EditorActions";
import TextSaveInput from "../../../common/component/TextSaveInput";
import CheckboxSaveInput from "../../../common/component/CheckBoxSaveInput";

const mapDispatchToProps = (dispatch) => {
    return {
        save: (field, value) => {
            dispatch({type: EDIT_SITE_DATA, field: field, value: value});
        },
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
                <div className="col-lg-7">
                    <div className="row form-group">
                            <label htmlFor="site_name" className="col-lg-4 control-label text-muted" >Name</label>
                            <div className="col-lg-8">
                                <TextSaveInput id="site_name" className="form-control" placeholder="Display name"
                                               value={siteData.name} save={(value) => save("name", value)}/>
                            </div>
                    </div>
                    <div className="row form-group">
                            <label htmlFor="start_node" className="col-lg-4 control-label text-muted">Start node</label>
                            <div className="col-lg-5">
                                <TextSaveInput id="start_node" className="form-control" placeholder="Network Id"
                                               value={siteData.startNodeNetworkId}
                                               save={(value) => save("startNode", value)}/>
                            </div>
                    </div>




                </div>
                <div className="col-lg-5">
                    <div className="row form-group">
                            <label htmlFor="hack_time" className="col-lg-5 control-label text-muted">Hack time</label>
                            <div className="col-lg-5">
                                <TextSaveInput id="hack_time" className="form-control" placeholder="(mm:ss)"
                                               value={siteData.hackTime} save={(value) => save("hackTime", value)}/>
                            </div>
                    </div>
                    <div className="row form-group">
                            <label htmlFor="site_hackable"
                                   className="col-lg-5 control-label text-muted">Hackable</label>
                            <div className="col-lg-2">
                                <CheckboxSaveInput id="site_hackable" className="form-check-input input-checkbox"
                                                   checked={siteData.hackable}
                                                   save={(value) => save("hackable", value)}/>
                            </div>
                    </div>
                </div>
            </div>
    </span>
        );
    });
