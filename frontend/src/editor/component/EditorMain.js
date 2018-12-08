import React from 'react';
import {connect} from "react-redux";
import NodesPanel from "./NodesPanel";
import CanvasPanel from "./CanvasPanel";
import DetailPanel from "./DetailPanel";

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
                <CanvasPanel dragAndDropState={dragAndDropState} dispatch={dispatch} />
                <DetailPanel/>
            </div>
        </span>
        );
    });
