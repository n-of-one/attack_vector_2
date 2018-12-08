import React from 'react';
import {connect} from "react-redux";
import ActionsPanel from "./ActionsPanel";
import NodeDetailsPanel from "./service/NodeDetailsPanel";

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
                <div className="col-lg-4" id="node-library">
                    <ActionsPanel/>
                    <div className="row">&nbsp;</div>
                    <NodeDetailsPanel />
                </div>
            </span>
        );
    });
