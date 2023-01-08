import React from 'react';
import {connect} from "react-redux";
import ActionsPanel from "./ActionsPanel";
import NodeDetailsPanel from "./layer/NodeDetailsPanel";
import LayersPanel from "./LayersPanel";

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {
    return {};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {
        return (
            <>
                <div className="col-lg-5" id="node-library">
                    <ActionsPanel/>
                    <div className="row">&nbsp;</div>
                    <LayersPanel />
                    <div className="row">&nbsp;</div>
                    <NodeDetailsPanel />
                </div>
            </>
        );
    });
