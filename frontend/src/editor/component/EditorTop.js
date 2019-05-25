import React from 'react';
import {connect} from "react-redux";
import SiteData from "./SiteData";
import SiteState from "./SiteState";

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
                    <div className="row">
                        <div className="col-lg-6" >
                            <SiteData />
                        </div>
                        <div className="col-lg-6" >
                            <SiteState />
                        </div>
                    </div>
                </div>
            </div>
        );
    });

