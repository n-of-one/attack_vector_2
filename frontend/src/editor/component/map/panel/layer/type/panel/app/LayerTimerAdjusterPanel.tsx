import React from 'react'
import {useDispatch} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {LayerTimerAdjuster} from "../../../../../../../../common/model/layer/LayerTimerAdjuster";
import {AttributeDropdown} from "../../../element/AttributeDropdown";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerTimerAdjusterPanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const layerTimerAdjuster = new LayerTimerAdjuster(layer, node, dispatch)


    return (
        <LayerPanel typeDisplay="Countdown Adjuster" layerObject={layerTimerAdjuster}>

            <AttributeDropdown label="Adjustment" value={layerTimerAdjuster.adjustmentType.toString()}
                               options={[{value: "SPEED_UP", text: "Speed up countdown"},
                                   {value: "SLOW_DOWN", text: "Slow down countdown"}]} save={(value: string) => layerTimerAdjuster.saveAdjustmentType(value)}
                               tooltipText="text" tooltipId="Adjustment"/>

            <TextAttribute size="medium" label="Amount" value={layerTimerAdjuster.amount}
                           save={value => layerTimerAdjuster.saveAmount(value)}
                           placeholder="(Timer speedup)"
                           help="When a hacker arrives at a node with this layer, all shutdown timers speed up by the amount chosen here."/>

            <AttributeDropdown label="Recurring" value={layerTimerAdjuster.recurring.toString()} options={
                [{value: "FIRST_ENTRY_ONLY", text: "First entry only"},
                    {value: "EVERY_ENTRY", text: "Every entry"},
                    {value: "EACH_HACKER_ONCE", text: "Each hacker once"},
                ]} save={(value: string) => layerTimerAdjuster.saveAdjustmentRecurring(value)} tooltipText="text" tooltipId="Adjustment"/>


        </LayerPanel>
    )
}
