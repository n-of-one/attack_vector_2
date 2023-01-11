import React from 'react';
import {connect} from "react-redux";
import SiteDataAdditional from "./SiteDataAdditional";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {
        return (
            <div className="row editorRow">
                <div className="col-lg-12 darkWell editorBottomPanel" >
                    <br/>
                    <SiteDataAdditional />
                </div>
            </div>
        );
    });

