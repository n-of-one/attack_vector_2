import React from 'react';
import {connect} from "react-redux";
import ServiceDataOs from "./ServiceDataOs";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {
        return (
            <div className="row form-horizontal dark_well serviceLayerPanel">
                <div className="row">&nbsp;</div>

                <div className="col-lg-12">
                    <ul className="nav nav-tabs" role="tablist" id="node-services-tab-list">
                        <li role="presentation" className="active">
                            <a aria-controls="home" role="tab" data-toggle="tab">OS</a>
                        </li>
                    </ul>
                    <br/>
                    <ServiceDataOs />
                </div>
            </div>
        );
    });
