import React from 'react';
import {connect} from "react-redux";
import ServiceDataOs from "./ServiceDataOs";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {
        return (
            <div className="row form-horizontal darkWell serviceLayerPanel">
                <div className="row">&nbsp;</div>

                <div className="col-lg-12">
                    <ul className="nav nav-tabs" role="tablist" id="node-services-tab-list">
                        <li role="presentation" className="active">
                            <a href="#" aria-controls="home" role="tab" data-toggle="tab">OS</a>
                        </li>
                    </ul>
                    <br/>
                    <ServiceDataOs />
                </div>
            </div>
        );
    });
