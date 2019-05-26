import React from 'react';
import {connect} from "react-redux";
import ServiceField from "../ServiceField";
import ServiceLayer from "../ServiceLayer";
import ServiceName from "../ServiceName";
import ServiceOs from "./ServiceOs";
import {findElementById} from "../../../../common/Immutable";

/* eslint jsx-a11y/alt-text: 0*/


const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    }
};
let mapStateToProps = (state) => {

    return {
        nodes: state.nodes,
        currentNodeId: state.currentNodeId,
        currentServiceId: state.currentServiceId,

    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({nodes, currentNodeId, currentServiceId, dispatch}) => {
        if (!currentNodeId) {
            return <div/>
        }

        const node = findElementById(nodes, currentNodeId);
        const service = findElementById(node.services, currentServiceId);

        const os = new ServiceOs(service, node, dispatch);

        // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
        const key = param => currentServiceId + ":" + param;

        if (node) {
            return (
                <div className="tab-content" id="node-services-tab-content ">
                    <div className="tab-pane active">
                        <ServiceName name="Node OS" node={node} service={service}/>
                        <ServiceLayer layer={service.layer} layourCount={node.services.length}/>
                        <ServiceField key={key("id")} size="small" name="Node id" value={os.id} readOnly={true} />
                        <ServiceField key={key("nw")} size="small" name="Network ▣" value={os.networkId} placeHolder="00" save={value => os.saveNetworkId(value)}/>
                        <ServiceField key={key("nn")} size="large" name="Node name" value={os.name} placeHolder="Optional name" save={value => os.saveName(value)} />
                        <ServiceField key={key("gm")} size="large" name="Gm Note" value={os.note} placeHolder="Only GM can read" save={value => os.saveNote(value)}/>
                    </div>
                </div>
            );
        }
        else {
            return (<div />);
        }


    });
