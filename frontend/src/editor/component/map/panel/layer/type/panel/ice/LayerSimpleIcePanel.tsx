import React from 'react'
import { useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {AttributeIceStrength} from "../../../element/AttributeIceStrength"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {AttributeIceUrlWithQr} from "../../../element/AttributeIceUrlWithQr";
import {LayerIce} from "../../../../../../../../common/model/layer/LayerIce";

interface Props {
    node: NodeI,
    layer: LayerDetails,
    typeDisplay: string
}

export const LayerSimpleIcePanel = ({node, layer, typeDisplay}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerIce(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay={typeDisplay} layerObject={ice}>
            <AttributeIceStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <AttributeIceUrlWithQr layerId={layer.id}/>
        </LayerPanel>
    )
}
