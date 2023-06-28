import React from 'react'
import { useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerStrength} from "../../../element/LayerStrength"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerField} from "../../../element/LayerField";
import {LayerSlowIce} from "../../../../../../../../common/model/layer/LayerSlowIce";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerSlowIcePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerSlowIce(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="ICE Slow" layerObject={ice}>
            <LayerStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <LayerField label="Units" size="small" value={ice.totalUnits} help="Total units to hack. Speed = (10 + level) per second per hacker" readOnly={true}/>
            <LayerField label="1 level 1" size="large" value={ice.time1Level1Hacker} help="Time it takes a single level 1 hacker to hack this ice." readOnly={true}/>
            <LayerField label="1 level 5" size="large" value={ice.time1Level5Hacker} help="Time it takes a single level 5 hacker to hack this ice." readOnly={true}/>
            <LayerField label="5 level 10" size="large" value={ice.time5Level10Hackers} help="Time it takes five level 10 hackers to hack this ice." readOnly={true}/>
        </LayerPanel>
    )
}
