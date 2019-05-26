import React from 'react';
import {connect} from "react-redux";
import ServiceOsPanel from "./type/ServiceOsPanel";
import {findElementById} from "../../../common/Immutable";
import {OS, TEXT} from "./ServiceTypes";
import ServiceTextPanel from "./type/ServiceTextPanel";
import SilentLink from "../../../common/component/SilentLink";
import {SELECT_SERVICE} from "../../EditorActions";
import Glyphicon from "../../../common/component/Glyphicon";

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
    const currentService = findElementById(node.services, state.currentServiceId);
    return {services: node.services, currentService: currentService};
};

const renderService = (serviceType) => {
    switch (serviceType) {
        case null:
            return <> </>;
        case OS:
            return <ServiceOsPanel/>;
        case TEXT:
            return <ServiceTextPanel/>;
        default:
            return <div className="text">ERROR: service type unknown: {serviceType}</div>
    }
};


const renderTab = (service, currentService, selectService) => {
    const activeClassName = (service === currentService) ? "active" : "";

    return (
        <li role="presentation" className={activeClassName}>
            <SilentLink onClick={() => selectService(service)} aria-controls="home" role="tab" data-toggle="tab">
                <Glyphicon type={service.type} size="18px" />
            </SilentLink>
        </li>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({services, currentService, selectService}) => {
        if (!currentService) {
            return <> </>;
        }
        return (
            <div className="row form-horizontal darkWell serviceLayerPanel">
                <div className="row">&nbsp;</div>

                <div className="col-lg-12">
                    <ul className="nav nav-tabs" role="tablist" id="node-services-tab-list">
                        {services.map(service => renderTab(service, currentService, selectService))}
                    </ul>
                    <br/>
                    {renderService(currentService.type)}
                </div>
            </div>
        );
    });
