import {OverlayTrigger, Tooltip} from "react-bootstrap";
import React from "react";
import {EditorState} from "../../../../../EditorRootReducer";
import {useSelector} from "react-redux";
import {LayerDetails, NodeI} from "../../../../../../common/sites/SiteModel";
import {avEncodedPath} from "../../../../../../common/util/PathEncodeUtils";

/* eslint react/jsx-no-target-blank  : 0*/

interface Props {
    node: NodeI,
    layer: LayerDetails,
    label: string,
    size: string,
    keyText: string
}

export const TextEditorLink = ({node, layer, label, size, keyText}: Props) => {

    const siteId = useSelector((state: EditorState) => state.siteProperties.siteId)
    const encodedPath = avEncodedPath(`LAYER|${siteId}|${node.id}|${layer.id}|${keyText}|node ${node.networkId} layer ${layer.level}`)

    let colSize = (size === "large") ? "col-lg-8 noRightPadding" : "col-lg-5 noRightPadding"
    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">{label}</div>
            <div className={colSize}>
                <a href={`/editText/${encodedPath}`} target="_blank">(fullscreen
                    editor with formatting)</a>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <OverlayTrigger
                    key={"tooltip_" + label}
                    placement="left"
                    overlay={
                        <Tooltip id={`tooltip-${label}`}>
                            Opens a new page that allows full screen editing of the text. Also show formatting options. This is useful when using formatting.
                        </Tooltip>
                    }
                >
                    <span className="badge bg-secondary helpBadge">?</span>
                </OverlayTrigger>
            </div>
        </div>
    )
}
