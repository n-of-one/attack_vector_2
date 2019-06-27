import React from 'react';
import {connect} from "react-redux";
import ServiceOsPanel from "./type/panel/ServiceOsPanel";
import {findElementById} from "../../../../../common/Immutable";
import {ICE_PASSWORD, OS, TEXT} from "./ServiceTypes";
import ServiceTextPanel from "./type/panel/ServiceTextPanel";
import SilentLink from "../../../../../common/component/SilentLink";
import {SELECT_SERVICE} from "../../../../EditorActions";
import Glyphicon from "../../../../../common/component/Glyphicon";
import ServiceIcePasswordPanel from "./type/panel/ServiceIcePasswordPanel";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        selectService: service => dispatch({type: SELECT_SERVICE, serviceId: service.id}),
    };
};

let mapStateToProps = (state) => {
    if (state.currentNodeId == null) {
        return {services: [], currentService: null};
    }
    const node = findElementById(state.nodes, state.currentNodeId);
    const service = findElementById(node.services, state.currentServiceId);
    return {
        node: node,
        services: node.services,
        currentService: service};
};

const renderService = (node, service) => {

    switch (service.type) {
        case null:
            return null;
        case OS:
            return <ServiceOsPanel node={node} service={service}/>;
        case TEXT:
            return <ServiceTextPanel node={node} service={service}/>;
        case ICE_PASSWORD:
            return <ServiceIcePasswordPanel node={node} service={service}/>;
        default:
            return <div className="text">NodeDetailPanel: ERROR: service type unknown: {service.type} for {service.id}</div>
    }
};


const renderTab = (service, currentService, selectService) => {
    const activeClassName = (service === currentService) ? "active" : "";

    return (
        <li role="presentation" className={activeClassName} key={service.id}>
            <SilentLink onClick={() => selectService(service)} aria-controls="home" role="tab" data-toggle="tab">
                <Glyphicon type={service.type} size="18px" />
            </SilentLink>
        </li>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, services, currentService, selectService}) => {
        if (!currentService) {
            return  <div className="row form-horizontal darkWell serviceLayerPanel" />;
        }
        return (
            <div className="row form-horizontal darkWell serviceLayerPanel">
                <div className="row">&nbsp;</div>

                <div className="col-lg-12">
                    <ul className="nav nav-tabs" role="tablist" id="node-services-tab-list">
                        {services.map(service => renderTab(service, currentService, selectService))}
                    </ul>
                    <br/>
                    {renderService(node, currentService)}
                </div>
            </div>
        );
    });
