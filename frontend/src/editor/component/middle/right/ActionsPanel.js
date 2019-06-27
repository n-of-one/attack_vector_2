import React from 'react';
import {connect} from "react-redux";
import {DELETE_CONNECTIONS, DELETE_NODE, SNAP} from "../../../EditorActions";
import editorCanvas from "../middle/EditorCanvas";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch,
    }
};

let mapStateToProps = (state) => {
    return { state: state};
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({dispatch}) => {

        let deleteConnections = () => {
            let nodeId = editorCanvas.getNodeSelectedId();
            if (nodeId) {
                dispatch({type: DELETE_CONNECTIONS, nodeId: nodeId});
            }
        };

        let deleteNode = () => {
            let nodeId = editorCanvas.getNodeSelectedId();
            if (nodeId) {
                dispatch({type: DELETE_NODE, nodeId: nodeId});
            }
        };

        let snap = () => {
                dispatch({type: SNAP});
        };

        return (
            <span>
            <div className="row">
                <div className="col-lg-12 darkWell">
                    <br/>
                    <div>
                        <div className="btn btn-info btn-spaced" onClick={() => deleteConnections()}>Delete Lines</div>
                        <div className="btn btn-info btn-spaced" onClick={() => deleteNode()}>Delete Node</div>
                        <div className="btn btn-info btn-spaced" onClick={() => snap()}>Snap</div>
                        <div className="btn btn-info disabled btn_tooltip btn-spaced" data-placement="bottom"
                             title="Control-click new node to connect with current selected node.">Connect</div>
                    </div>
                    <br/>
                </div>
            </div>
        </span>
        );
    });
