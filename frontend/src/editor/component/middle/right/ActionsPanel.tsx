import React from 'react'
import {editorCanvas} from "../middle/EditorCanvas"
import {OverlayTrigger, Tooltip} from "react-bootstrap"
import {sendDeleteConnections, sendDeleteNode, sendSnap} from "../../../server/EditorServerClient"


export const ActionsPanel = () => {

    const deleteConnections = () => {
        let nodeId = editorCanvas.getNodeSelectedId()

        if (nodeId) {
            sendDeleteConnections({nodeId})
        }
    }

    const deleteNode = () => {
        let nodeId = editorCanvas.getNodeSelectedId()
        if (nodeId) {
            sendDeleteNode({nodeId})
        }
    }

    const snap = () => {
        sendSnap()
    }

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
    )
}
