import React from 'react'
import {useDispatch} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {LayerShutdownAccelerator} from "../../../../../../../../common/model/layer/LayerShutdownAccelerator";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerShutdownAcceleratorPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const layerShutdownAccelerator = new LayerShutdownAccelerator(layer, node, dispatch)




    return (
        <LayerPanel typeDisplay="Shutdown Accelerator" layerObject={layerShutdownAccelerator}>

            <TextAttribute size="large" label="Countdown" value={layerShutdownAccelerator.increase}
                           save={value => layerShutdownAccelerator.saveIncrease(value)}
                           placeholder="(Timer speedup)" help="When a hacker arrives at a node with this layer, all shutdown timers speed up by the amount chosen here."/>


        </LayerPanel>
    )
}
