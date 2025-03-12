import React from 'react'
import {useDispatch} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {AttributeIceStrength} from "../../../element/AttributeIceStrength"
import {AttributeIceUrlWithQr} from "../../../element/AttributeIceUrlWithQr";
import {AttributeDropdown} from "../../../element/AttributeDropdown";
import {LayerIceTangle} from "../../../../../../../../common/model/layer/LayerIceTangle";
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";

interface Props {
    node: NodeI,
    layer: LayerDetails,
    typeDisplay: string
}

const clusterOptions = [{value: "1", text: "1"}, {value: "2", text: "2"}, {value: "3", text: "3"},
    {value: "4", text: "4"},]

export const TangleIcePanel = ({node, layer, typeDisplay}: Props) => {

    const dispatch = useDispatch()
    const ice = new LayerIceTangle(layer, node, dispatch)

    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay={typeDisplay} layerObject={ice}>
            <AttributeIceStrength key={key("strength")} value={ice.strength}
                                  save={(value: string) => ice.saveStrength(value)}/>
            <AttributeDropdown key={key("clusters")} label="Clusters" value={ice.clusters} options={clusterOptions}
                               save={(value: string) => ice.saveClusters(value)}
                               tooltipText="Number of separate puzzles, or clusters of points. More clusters allow more players to work together to solve it."
                               tooltipId="clusters"/>
            <AttributeIceUrlWithQr layerId={layer.id}/>
        </LayerPanel>
    )
}
