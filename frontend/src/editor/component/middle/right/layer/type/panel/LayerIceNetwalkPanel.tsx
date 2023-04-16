import React from 'react'
import { useDispatch} from "react-redux"
import {LayerPanel} from "./LayerPanel"
import {LayerStrength} from "../../LayerStrength"
import {LayerIceTangle} from "../../../../../../../common/model/layer/LayerIceTangle"
import {LayerDetails, NodeI} from "../../../../../../reducer/NodesReducer"

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerIceNetWalkPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerIceTangle(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="ICE Netwalk" layerObject={ice}>
            <LayerStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
        </LayerPanel>
    )
}
