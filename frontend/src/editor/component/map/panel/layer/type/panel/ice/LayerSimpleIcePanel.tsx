import React from 'react'
import {useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {AttributeIceStrength} from "../../../element/AttributeIceStrength"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {AttributeIceUrlWithQr} from "../../../element/AttributeIceUrlWithQr";
import {LayerIce} from "../../../../../../../../common/model/layer/LayerIce";
import {iceSimpleName, IceType} from "../../../../../../../../common/enums/LayerTypes";

interface Props {
    node: NodeI,
    layer: LayerDetails,
    typeDisplay: string
}

export const LayerSimpleIcePanel = ({node, layer, typeDisplay}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerIce(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    const simpleName = iceSimpleName[layer.type as unknown as IceType].toLowerCase().replace(" ", "-")
    const fileName = `node-${node.networkId}-layer-${layer.level}-${simpleName}`

    return (
        <LayerPanel typeDisplay={typeDisplay} layerObject={ice}>
            <AttributeIceStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <AttributeIceUrlWithQr layerId={layer.id} fileName={fileName}/>
        </LayerPanel>
    )
}
