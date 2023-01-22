import React from 'react';
import {useDispatch} from "react-redux";
import {SWAP_LAYERS} from "../../../../EditorActions";
import SilentLink from "../../../../../common/component/SilentLink";
import {EditorLayerDetails, Node} from "../../../../reducer/NodesReducer";

/* eslint jsx-a11y/anchor-is-valid: 0*/

interface Props {
    node: Node,
    layer: EditorLayerDetails
}

export const LayerLevel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const swap = (node: Node, layer: EditorLayerDetails, level: number) => {
        dispatch({type: SWAP_LAYERS, nodeId: node.id, fromId: layer.id, toId: node.layers[level].id});
    };

    const up = (node: Node, layer: EditorLayerDetails) => swap(node, layer, layer.level - 1)
    const down = (node: Node, layer: EditorLayerDetails) => swap(node, layer, layer.level + 1)

    const level = layer.level;

    let downClickable = (level > 1);
    let downHtml = downClickable ? (
        <SilentLink classNameInput="textLink" onClick={() => up(node, layer)}><>◀</></SilentLink>) : (<span>◀</span>
    );

    let upClickable = (level !== 0 && level < (node.layers.length - 1));
    let upHtml = upClickable ? (
        <SilentLink classNameInput="textLink" onClick={() => down(node, layer)}><>▶</></SilentLink>) : (<span>▶</span>
    );

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">Layer</div>
            <div className="col-lg-8">
                <div className="text-muted strong layer_text_label">
                    {level}&nbsp;{downHtml}&nbsp;{upHtml}
                </div>
            </div>
        </div>
    );
}
