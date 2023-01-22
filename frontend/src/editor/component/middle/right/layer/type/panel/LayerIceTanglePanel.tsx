import React from 'react';
import {connect, useDispatch} from "react-redux";
import {LayerPanel} from "./LayerPanel";
import LayerStrength from "../../LayerStrength";
import LayerIceTangle from "../../../../../../../common/model/layer/LayerIceTangle";
import {EditorLayerDetails, Node} from "../../../../../../reducer/NodesReducer";
import LayerText from "../../../../../../../common/model/layer/LayerText";

interface Props {
    node: Node,
    layer: EditorLayerDetails
}

export const LayerIceTanglePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerIceTangle(layer, node, dispatch);

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param;

    return (
        <LayerPanel typeDisplay="ICE (Un)tangle" layerObject={ice}>
            <LayerStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
        </LayerPanel>
    );
}
