import React from 'react';
import {connect} from "react-redux";
import {findElementById} from "../../../../../../../common/Immutable";
import ServiceType from "../../ServiceName";
import ServiceLayer from "../../ServiceLayer";
import ServiceField from "../../ServiceField";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    }
};
let mapStateToProps = (state) => {
    if (!state.currentNodeId) {
        return {}
    }
    const node = findElementById(state.nodes, state.currentNodeId);
    const service = findElementById(node.services, state.currentServiceId);

    return {
        node: node,
        serviceData: service,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({type, node, serviceData, serviceObject, children}) => {
        if (!node) {
            return <div/>
        }

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => serviceData.id + ":" + param;

        return (
            <div className="tab-content" id="node-services-tab-content ">
                <div className="tab-pane active">
                    <ServiceType type={type} node={node} service={serviceData}/>
                    <ServiceLayer service={serviceData} node={node}/>
                    <ServiceField key={key("id")} size="small" name="Service id" value={serviceObject.id} readOnly={true}
                                  help="Unique ID of this service. Used when services refer to each other."/>
                    <ServiceField key={key("nn")} size="small" name="Service name" value={serviceObject.name} save={value => serviceObject.saveName(value)}
                                  placeholder="As seen by hackers" help="When a hacker 'scans' or 'views' a node they will see the services by this name." />
                    {children}
                    <ServiceField key={key("no")} size="large" name="Gm Note" value={serviceObject.note} save={value => serviceObject.saveNote(value)}
                                  placeholder="" help="Players will never see this. Notes can help to understand the design of a site."/>
                </div>
            </div>
        );
    });

