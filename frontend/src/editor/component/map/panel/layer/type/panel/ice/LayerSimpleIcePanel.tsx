import React from 'react'
import { useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerStrength} from "../../../element/LayerStrength"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {IceUrlFieldWithQr} from "../../../element/IceUrlFieldWithQr";
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
            <LayerStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <IceUrlFieldWithQr layerId={layer.id}/>
        </LayerPanel>
    )
}
