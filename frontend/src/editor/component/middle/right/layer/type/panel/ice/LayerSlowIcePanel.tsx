import React from 'react'
import { useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerStrength} from "../../../element/LayerStrength"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerField} from "../../../element/LayerField";
import {LayerSlowIce} from "../../../../../../../../common/model/layer/LayerSlowIce";
import {UrlFieldWithQr} from "../../../element/UrlFieldWithQr";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerSlowIcePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerSlowIce(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    const textOneHacker = `1x lvl 1: ${ice.time1Level1Hacker}s   1x lvl 5: ${ice.time1Level5Hacker}s`
    const textFivesHacker = `5x lvl 10: ${ice.time5Level10Hackers}s`
    return (
        <LayerPanel typeDisplay="ICE Slow" layerObject={ice}>
            <LayerStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <UrlFieldWithQr name="URL" type="ice" id={layer.id} description="App for changing status"/>
            <LayerField label="Units" size="small" value={ice.totalUnits} help="Total units to hack. Speed = (10 + level) per second per hacker" readOnly={true}/>
            <LayerField label="1 hacker" size="large" value={textOneHacker} help="Time it takes a single hacker to hack this ice." readOnly={true}/>
            <LayerField label="Max hacking" size="large" value={textFivesHacker} help="Time it takes five level 10 hackers to hack this ice." readOnly={true}/>
        </LayerPanel>
    )
}
