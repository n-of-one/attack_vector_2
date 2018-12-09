import React from 'react';
import {connect} from "react-redux";
import ServiceField from "./ServiceField";
import ServiceLayer from "./ServiceLayer";
import ServiceName from "./ServiceName";

/* eslint jsx-a11y/alt-text: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
    }
};
let mapStateToProps = (state) => {

    return {
        currentNodeId: state.currentNodeId,
        currentNode: state.nodes.find((node) => { return node.id === state.currentNodeId})

    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentNodeId, currentNode}) => {

        if (currentNodeId) {
            return (
                <div className="tab-content" id="node-services-tab-content ">
                    <div className="tab-pane active">
                        <ServiceName name="Node OS"/>
                        <ServiceLayer layer={0} layourCount={1}/>
                        <ServiceField type="small" name="Node id" value={currentNodeId} readOnly={true}/>
                        <ServiceField type="small" name="Network ▣" value={currentNode.services[0].data["networkId"]}/>
                        <ServiceField type="large" name="Node name"/>
                        <ServiceField type="large" name="Gm Note"/>
                    </div>
                </div>
            );
        }
        else {
            return (<div />);
        }


    });
