import React from 'react'
import {useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {AttributeIceStrength} from "../../../element/AttributeIceStrength"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
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

    const minutes1Hacker = Math.floor(ice.totalUnits / (15 * 60))
    const minutes2Hackers = Math.floor(ice.totalUnits / (15 * 60 * 2))
    const minutes5Hackers = Math.floor(ice.totalUnits / (15 * 60 * 5))

    return (
        <LayerPanel typeDisplay="ICE Tar" layerObject={ice}>
            <AttributeIceStrength key={key("strength")} value={ice.strength} save={(value: string) => ice.saveStrength(value)}/>
            <AttributeIceUrlWithQr layerId={layer.id}/>
            <TextAttribute label="Units" size="small" value={ice.totalUnits} help="Total units to hack. Speed = 15 units per second per hacker" readOnly={true}/>
            <TextAttribute label="Total time" size="large" value="See question mark to the right"
                           help={`It will take 1 hacker: ${minutes1Hacker} minutes to hack.
                        It will take 2 hackers: ${minutes2Hackers} minutes to hack.
                        It will take 5 hackers: ${minutes5Hackers} minutes to hack.
                        `} readOnly={true}/>
        </LayerPanel>
    )
}
