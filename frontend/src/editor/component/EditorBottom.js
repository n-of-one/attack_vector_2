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
            <div className="row">
                <div className="col-lg-12 darkWell" >
                    <br/>
                    <SiteDataAdditional />
                </div>
            </div>
        );
    });
