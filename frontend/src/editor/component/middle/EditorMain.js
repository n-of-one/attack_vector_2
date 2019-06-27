import React from 'react';
import {connect} from "react-redux";
import NodesPanel from "./left/NodesPanel";
import EditCanvasPanel from "./middle/EditCanvasPanel";
import DetailPanel from "./right/DetailPanel";

let mapStateToProps = (state) => {
    return {
        dragAndDropState: state.dragAndDrop,
    };
};

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({dragAndDropState, dispatch}) => {
        return (
        <span>
            <div className="row">
                <NodesPanel />
                <EditCanvasPanel dragAndDropState={dragAndDropState} dispatch={dispatch} />
                <DetailPanel/>
            </div>
        </span>
        );
    });
