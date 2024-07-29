import React from 'react'
import { useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {AttributeIceStrength} from "../../../element/AttributeIceStrength"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {TextAttribute} from "../../../element/TextAttribute";
import {LayerIceTar} from "../../../../../../../../common/model/layer/LayerIceTar";
import {AttributeIceUrlWithQr} from "../../../element/AttributeIceUrlWithQr";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerIceTarPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerIceTar(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="ICE Tar" layerObject={ice}>
            <AttributeIceStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <AttributeIceUrlWithQr layerId={layer.id}/>
            <TextAttribute label="Units" size="small" value={ice.totalUnits} help="Total units to hack. Speed = (10 + level) per second per hacker" readOnly={true}/>
            <TextAttribute label="Total time" size="large" value="See question mark to the right"
                           help={`It will take 1 lvl 1 hacker: ${ice.time1Level1Hacker} to hack.
                        It will take 1 lvl 5 hacker: ${ice.time1Level5Hacker} to hack.
                        It will take 5 lvl 10 hackers: ${ice.time5Level10Hackers} to hack.
                        `} readOnly={true}/>
        </LayerPanel>
    )
}
