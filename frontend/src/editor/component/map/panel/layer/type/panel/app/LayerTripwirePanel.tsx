import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerField} from "../../../element/LayerField"
import {LayerPanel} from "../LayerPanel"
import {LayerTripwire} from "../../../../../../../../common/model/layer/LayerTripwire"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {EditorState} from "../../../../../../../EditorRootReducer";
import {coreLayerOptions} from "./LayerUtil";
import {LayerFieldDropdown} from "../../../element/LayerFieldDropdown";
import {editorCanvas} from "../../../../../canvas/EditorCanvas";
import {SELECT_LAYER} from "../../../../../../../reducer/CurrentLayerIdReducer";

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerTripwirePanel = ({node, layer}: Props) => {

    const dispatch = useDispatch()

    const layerTripwire = new LayerTripwire(layer, node, dispatch)
    const nodes = useSelector((state: EditorState) => state.nodes)
    const options = coreLayerOptions(nodes)

    const coreLayerId = layerTripwire.coreLayerId
    const navigateToLayer = () => {
        const nodeId = coreLayerId.split(":")[0]
        editorCanvas.selectNode(nodeId)
        dispatch({type: SELECT_LAYER, layerId: coreLayerId})
    }
    const navigateIfCoreId = (coreLayerId) ? navigateToLayer : undefined



    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="Tripwire" layerObject={layerTripwire}>
            <LayerFieldDropdown key={key("status")} label="Reset by"
                                value={layerTripwire.coreLayerId }
                                options={options}
                                save={value => layerTripwire.saveCoreLayerId(value)}
                                tooltipId="forIce" tooltipText="The core that can reset this timer"
                                navigate={navigateIfCoreId}
            />            <LayerField key={key("countdown")} size="large" label="Countdown" value={layerTripwire.countdown}
                        save={value => layerTripwire.saveCountdown(value)}
                        placeholder="(Minutes until alarm)" help="Minutes part of time until alarm."/>

            <LayerField key={key("shutdown")} size="large" label="Shutdown" value={layerTripwire.shutdown}
                        save={value => layerTripwire.saveShutdown(value)}
                        placeholder="(Minutes of shutdown)" help="Duration of shutdown."/>

        </LayerPanel>
    )
}
