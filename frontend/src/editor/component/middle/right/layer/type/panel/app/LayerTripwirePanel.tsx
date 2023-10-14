import React from 'react'
import {useDispatch} from "react-redux"
import {LayerField} from "../../../element/LayerField"
import {LayerPanel} from "../LayerPanel"
import {LayerTripwire} from "../../../../../../../../common/model/layer/LayerTripwire"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerTripwirePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const layerObject = new LayerTripwire(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="Timer Trigger" layerObject={layerObject}>
            <LayerField key={key("countdown")} size="large" label="Countdown" value={layerObject.countdown}
                        save={value => layerObject.saveCountdown(value)}
                        placeholder="(Minutes until alarm)" help="Minutes part of time until alarm."/>

            <LayerField key={key("shutdown")} size="large" label="Shutdown" value={layerObject.shutdown}
                        save={value => layerObject.saveShutdown(value)}
                        placeholder="(Minutes of shutdown)" help="Duration of shutdown."/>
        </LayerPanel>
    )
}
