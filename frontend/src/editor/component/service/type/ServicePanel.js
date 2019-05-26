import React from 'react';
import {connect} from "react-redux";
import {findElementById} from "../../../../common/Immutable";
import ServiceName from "../ServiceName";
import ServiceLayer from "../ServiceLayer";
import ServiceField from "../ServiceField";

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
        currentNodeId: state.currentNodeId,
        currentServiceId: state.currentServiceId,
        node: node,
        service: service,

    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({name, node, service, children}) => {
        if (!node) {
            return <div/>
        }

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => service.id + ":" + param;

        if (node) {
            return (
                <div className="tab-content" id="node-services-tab-content ">
                    <div className="tab-pane active">
                        <ServiceName name={name} node={node} service={service}/>
                        <ServiceLayer layer={service.layer} layourCount={node.services.length}/>
                        <ServiceField key={key("id")} size="small" name="Node id" value={service.id} readOnly={true} />
                        {children}
                    </div>
                </div>
            );
        }
        else {
            return (<div />);
        }
    });

