import React from 'react'
import {useDispatch} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerOs} from "../../../../../../../../common/model/layer/LayerOs"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerScriptInteraction} from "../../../../../../../../common/model/layer/LayerScriptInteraction";

interface Props {
    node: NodeI,
    layer: LayerDetails
}
export const LayerScriptInteractionPanel = ({node, layer} : Props) => {

    const dispatch = useDispatch()

    const scriptInteraction = new LayerScriptInteraction(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="Script Interaction" layerObject={scriptInteraction}>
            <TextAttribute key={key("network")} size="small" label="Interaction key" value={scriptInteraction.interactionKey} save={value => scriptInteraction.saveInteractionKey(value)}
                           help="The interaction key defines which script effects can interact with this layer.
                           A script that has an effect with the same interaction key as this layer will interact with this layer" placeholder="SCRIPT_UNIQUE_KEY" />
            <TextAttribute key={key("nodeName")} size="large" label="message" value={scriptInteraction.message} save={value => scriptInteraction.saveMessage(value)}
                           placeholder="Message" help="The message to display to the hacker if they run the appropriate script (with the correct interaction key) on this layer."/>
        </LayerPanel>
    )
}
