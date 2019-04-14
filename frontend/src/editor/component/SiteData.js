import React from 'react';
import {connect} from "react-redux";
import {EDIT_SITE_DATA} from "../EditorActions";
import TextSaveInput from "../../common/TextSaveInput";
import CheckboxSaveInput from "../../common/CheckBoxSaveInput";

const mapDispatchToProps = (dispatch) => {
    return {
        save: (field, value) => { dispatch({type: EDIT_SITE_DATA, field: field, value: value}); },
    }
};
let mapStateToProps = (state) => {
    return {
        site: state.site,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({site, save}) => {
    return (
    <div className="row">
        <div className="col-lg-11 darkWell">
            <br />
            <div className="row">
                <div className="col-lg-6">
                    <div className="row form-horizontal" >
                        <div className="form-group has-feedback">
                            <label htmlFor="site_name" className="col-lg-3 control-label text-muted">Name</label>
                            <div className="col-lg-8">
                                <TextSaveInput id="site_name" className="form-control" placeholder="Display name" value={site.name} save={ (value) => save("name", value) } />
                            </div>
                        </div>
                        <div className="form-group has-feedback">
                            <label htmlFor="site_description" className="col-lg-3 control-label text-muted">Description</label>
                            <div className="col-lg-8">
                                <TextSaveInput type="textArea" id="site_description" rows="2" className="form-control" placeholder="For GM only" value={site.description} save={ (value) => save("description", value) } />
                            </div>
                        </div>
                        <div className="form-group has-feedback">
                            <label htmlFor="gm_name" className="col-lg-3 control-label text-muted">Creator</label>
                            <div className="col-lg-8">
                                <TextSaveInput id="gm_name" className="form-control" placeholder="" value={site.creator} save={ (value) => save("creator", value) }/>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="col-lg-6">
                    <div className="row form-horizontal" >
                        <div className="form-group has-feedback">
                            <label htmlFor="hack_time" className="col-lg-3 control-label text-muted">Hack time</label>
                            <div className="col-lg-3">
                                <TextSaveInput id="hack_time" className="form-control" placeholder="(mm:ss)" value={site.hackTime} save={ (value) => save("hackTime", value) } />
                            </div>
                        </div>
                        <div className="form-group has-feedback">
                            <label htmlFor="start_node" className="col-lg-3 control-label text-muted">Start node</label>
                            <div className="col-lg-3">
                                <TextSaveInput id="start_node" className="form-control" placeholder="Network Id" value={site.startNodeId} save={ (value) => save("startNode", value) } />
                            </div>
                        </div>
                        <div className="form-group has-feedback">
                            <label htmlFor="site_hackable" className="col-lg-3 control-label text-muted">Site hackable</label>
                            <div className="col-lg-1 text-left">
                                <CheckboxSaveInput id="site_hackable" className="form-control" checked={site.hackable} save={ (value) => save("hackable", value) } />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    );
});
