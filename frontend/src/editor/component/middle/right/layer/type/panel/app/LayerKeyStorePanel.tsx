import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerFieldDropdown} from "../../../element/LayerFieldDropdown";
import {EditorState} from "../../../../../../../EditorRootReducer";
import {editorCanvas} from "../../../../../middle/EditorCanvas";
import {SELECT_LAYER} from "../../../../../../../reducer/CurrentLayerIdReducer";
import {LayerKeyStore} from "../../../../../../../../common/model/layer/LayerKeystore";


interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const LayerKeyStorePanel = ({node, layer}: Props) => {
    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    const dispatch = useDispatch()
    const keystore = new LayerKeyStore(layer, node, dispatch)
    const nodes = useSelector((state: EditorState) => state.nodes)
    const options = iceLayerOptions(nodes)

    const iceLayerId = keystore.iceLayerId
    const navigateToLayer = () => {
        const nodeId = iceLayerId.split(":")[0]
        editorCanvas.selectNode(nodeId)
        dispatch({type: SELECT_LAYER, layerId: keystore.iceLayerId})
    }
    const navigateIfIceId = (iceLayerId) ? navigateToLayer : undefined

    return (
        <LayerPanel typeDisplay="Keystore" layerObject={keystore}>
            <LayerFieldDropdown key={key("status")} label="For ICE"
                                value={keystore.iceLayerId }
                                options={options}
                                save={value => keystore.saveIceId(value)}
                                tooltipId="forIce" tooltipText="The ICE for which this keystore contains the password/key"
                                navigate={navigateIfIceId}
            />
        </LayerPanel>
    )
}


type Option = {
    value: string,
    text: string,
}

const iceLayerOptions = (nodes: Array<NodeI>): Option[] => {
    const iceLayers = nodes
        .map(node => {
            return node.layers.map(layer => { return {networkId: node.networkId, layer: layer}})
        })
        .flat()
        .filter(layerWithNetwork => layerWithNetwork.layer.ice)
        .sort((a, b) => a.layer.level - b.layer.level)
        .sort((a, b) => a.networkId.localeCompare(b.networkId))
        .map(layerWithNetwork => {
            const location = layerWithNetwork.networkId + ":" + layerWithNetwork.layer.level
            return {
                value: layerWithNetwork.layer.id,
                text: `${location} ${layerWithNetwork.layer.name} ${layerWithNetwork.layer.type}`,
            }
        })
    return [{value: "", text: "Choose Ice"}, ...iceLayers ]
}