import React from 'react';
import {connect} from "react-redux";
import ServiceOsPanel from "./type/ServiceOsPanel";
import {findElementById} from "../../../common/Immutable";
import {OS, TEXT} from "./ServiceTypes";
import ServiceTextPanel from "./type/ServiceTextPanel";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    if (state.currentNodeId == null) {
        return { serviceType: null };
    }
    const node = findElementById(state.nodes, state.currentNodeId);
    const service = findElementById(node.services, state.currentServiceId);
    return { serviceType: service.type };
};

const renderService = (serviceType) => {
    switch (serviceType) {
        case null: return <> </>;
        case OS: return <ServiceOsPanel />;
        case TEXT: return <ServiceTextPanel />;
        default: return <div className="text">ERROR: service type unknown: {serviceType}</div>
    }

};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({serviceType}) => {
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
                    { renderService(serviceType) }
                </div>
            </div>
        );
    });
