import React from 'react'
import {useDispatch} from "react-redux"
import {TextAttribute} from "../../../element/TextAttribute"
import {LayerPanel} from "../LayerPanel"
import {LayerOs} from "../../../../../../../../common/model/layer/LayerOs"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"

interface Props {
    node: NodeI,
    layer: LayerDetails
}
export const LayerOsPanel = ({node, layer} : Props) => {

    const dispatch = useDispatch()

    const os = new LayerOs(layer, node, dispatch)

    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    return (
        <LayerPanel typeDisplay="OS" layerObject={os}>
            <TextAttribute key={key("network")} size="small" label="Network ▣" value={os.networkId} save={value => os.saveNetworkId(value)}
                           help="This is the number shown next to the node icon.
                          It is used by the hacker to navigate through the site." placeholder="<xx>" />
            <TextAttribute key={key("nodeName")} size="large" label="Node name" value={os.nodeName} save={value => os.saveNodeName(value)}
                           placeholder="Optional name" help="When a node has a name, this is shown in the scan and when a hacker enters the node.
                          It can be used to give a node extra meaning."/>
        </LayerPanel>
    )
}
