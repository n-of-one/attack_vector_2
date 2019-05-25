import React from 'react';
import {connect} from "react-redux";
import ServiceField from "./ServiceField";
import ServiceLayer from "./ServiceLayer";
import ServiceName from "./ServiceName";
import ServiceOs from "./ServiceOs";
import {findElementById} from "../../../common/Immutable";

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

        const currentNode = findElementById(nodes, currentNodeId);
        const currentService = findElementById(currentNode.services, currentServiceId);

        const os = new ServiceOs(currentService, currentNode, dispatch);

        if (currentNode) {
            return (
                <div className="tab-content" id="node-services-tab-content ">
                    <div className="tab-pane active">
                        <ServiceName name="Node OS"/>
                        <ServiceLayer layer={0} layourCount={1}/>
                        <ServiceField size="small" name="Node id" value={os.id} readOnly={true} />
                        <ServiceField size="small" name="Network ▣" value={os.networkId} placeHolder="00" save={value => os.saveNetworkId(value)}/>
                        <ServiceField size="large" name="Node name" value={os.name} placeHolder="Optional name" save={value => os.saveName(value)} />
                        <ServiceField size="large" name="Gm Note" value={os.gmNote} placeHolder="Only GM can read" save={value => os.saveGmNote(value)}/>
                    </div>
                </div>
            );
        }
        else {
            return (<div />);
        }


    });
