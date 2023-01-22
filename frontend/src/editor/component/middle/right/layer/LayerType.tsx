import React from 'react';
import { useDispatch} from "react-redux";
import SilentLink from "../../../../../common/component/SilentLink";
import Glyphicon from "../../../../../common/component/Glyphicon";
import {OS} from "../../../../../common/enums/LayerTypes";
import {REMOVE_LAYER} from "../../../../EditorActions";
import {EditorLayerDetails, Node} from "../../../../reducer/NodesReducer";

const renderRemove = (node: Node, layer: EditorLayerDetails, remove: (nodeId: string, layerId: string) => void) => {
    if (layer.type === OS) {
        return null;
    }
    return (
        <span className="pull-right" style={{display: "block"}}>
            <SilentLink onClick={() => remove(node.id, layer.id)}>
                <Glyphicon name="glyphicon-remove" size="18px" display="block"/>
            </SilentLink>
        </span>
    );
};

interface Props {
    node: Node,
    layer: EditorLayerDetails,
    typeDisplay: string
}
export const LayerType = ({node, layer, typeDisplay}: Props) => {

    const dispatch = useDispatch();
    const remove = (nodeId: string, layerId: string) => dispatch({type: REMOVE_LAYER, nodeId: nodeId, layerId: layerId});

    return (
        <div className="row form-group layerFieldTopRow">
            <div className="col-lg-3 control-label layerLabel">Type</div>
            <div className="col-lg-8">
                <div className="strong layer_text_label text_gold d-flex justify-content-between">
                    <span>{typeDisplay}</span>
                    <span>{renderRemove(node, layer, remove)}</span>
                </div>
            </div>
        </div>
    );
}
