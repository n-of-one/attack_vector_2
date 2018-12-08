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
        currentNode: state.currentNode,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentNode}) => {

        let nodeId = (currentNode) ? currentNode : 12;

        return (
            <div className="tab-content" id="node-services-tab-content">
                <div className="tab-pane active">
                    <ServiceName name="Node OS"/>
                    <ServiceLayer layer={0} layourCount={1}/>
                    <ServiceField type="small" name="Node id" value={nodeId} readOnly={true}/>
                    <ServiceField type="small" name="Network ▣"/>
                    <ServiceField type="large" name="Node name"/>
                    <ServiceField type="large" name="Gm Note"/>
                </div>
            </div>
        );
    });
