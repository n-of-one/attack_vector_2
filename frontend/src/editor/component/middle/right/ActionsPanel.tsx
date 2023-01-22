import React from 'react';
import {useDispatch} from "react-redux";
import {DELETE_CONNECTIONS, DELETE_NODE, SNAP} from "../../../EditorActions";
import editorCanvas from "../middle/EditorCanvas";
import {OverlayTrigger, Tooltip} from "react-bootstrap";


export const ActionsPanel = () => {

    const dispatch = useDispatch();

    const deleteConnections = () => {
        let nodeId = editorCanvas.getNodeSelectedId();
        if (nodeId) {
            dispatch({type: DELETE_CONNECTIONS, nodeId: nodeId});
        }
    };

    const deleteNode = () => {
        let nodeId = editorCanvas.getNodeSelectedId();
        if (nodeId) {
            dispatch({type: DELETE_NODE, nodeId: nodeId});
        }
    };

    const snap = () => {
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
                            <OverlayTrigger
                                key="tooltip_action_connect" placement="top"
                                overlay={
                                    <Tooltip id="tooltip_action_connect">Control-click new node to connect with current selected node.</Tooltip>
                                }
                            >
                                <span className="btn btn-info helpBadge"
                                      style={{
                                          // Cannot use the disabled class because it also disables the tooltip.
                                          color: "var(--bs-btn-disabled-color)",
                                          opacity: "var(--bs-btn-disabled-opacity)"
                                      }}
                                >Connect</span>
                            </OverlayTrigger>
                    </div>
                    <br/>
                </div>
            </div>
        </span>
    );
}
