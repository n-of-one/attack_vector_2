import React from 'react';
import {connect} from "react-redux";
import ActionsPanel from "./ActionsPanel";
import NodeDetailsPanel from "./service/NodeDetailsPanel";
import ServicesPanel from "./ServicesPanel";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {
        return (
            <span>
                <div className="col-lg-5" id="node-library">
                    <ActionsPanel/>
                    <div className="row">&nbsp;</div>
                    <ServicesPanel />
                    <div className="row">&nbsp;</div>
                    <NodeDetailsPanel />
                </div>
            </span>
        );
    });
