import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerText} from "../../../../../../../../common/model/layer/LayerText"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../reducer/NodesReducer"
import {LayerFieldDropdown} from "../../../element/LayerFieldDropdown";
import {EditorState} from "../../../../../../../EditorRootReducer";
import {avEncodedUrl} from "../../../../../../../../common/util/Util";
import toast, {Toast} from "react-hot-toast";
import QRCode from "react-qr-code";
import {editorCanvas} from "../../../../../middle/EditorCanvas";
import {SELECT_LAYER} from "../../../../../../../reducer/CurrentLayerIdReducer";
import {SilentLink} from "../../../../../../../../common/component/SilentLink";
import {LayerKeyStore} from "../../../../../../../../common/model/layer/LayerKeystore";


interface Props {
    node: NodeI,
    layer: LayerDetails
}

const nop = () => {}

export const LayerKeyStorePanel = ({node, layer}: Props) => {
    // Unique key. See https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
    const key = (param: string) => layer.id + ":" + param

    const dispatch = useDispatch()
    const keystore = new LayerKeyStore(layer, node, dispatch)
    const nodes = useSelector((state: EditorState) => state.nodes)
    const options = deriveOptions(nodes)

    const iceId = keystore.iceId
    // const iceLayer = options.find(option => option.value === iceId)



    const navigateToLayer = (iceId) ? () => {
        const nodeId = iceId.split(":")[0]
        editorCanvas.selectNode(nodeId)
        // dispatch({type: SELECT_LAYER, layerId: nodes[0].layers[1].id})
        dispatch({type: SELECT_LAYER, layerId: keystore.iceId})
    } : undefined;



    return (
        <LayerPanel typeDisplay="Keystore" layerObject={keystore}>
            <LayerFieldDropdown key={key("status")} label="For ICE"
                                value={keystore.iceId }
                                options={options}
                                save={value => keystore.saveIceId(value)}
                                tooltipId="forIce" tooltipText="The ICE for which this keystore contains the password/key"
                                navigate={navigateToLayer}
            />

            {/*<LayerField key={key("text")} size="large" label="Hacked text" value={text.text} save={value => text.saveText(value)}*/}
            {/*            placeholder="* Data found: ..." help="This is the text displayed when a player hacks this layer.*/}
            {/*                  It can be used to provide data, or to simulate that some effect has taken place."/>*/}
        </LayerPanel>
    )
}


type Option = {
    value: string,
    text: string,
}


const deriveOptions = (nodes: Array<NodeI>): Option[] => {
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
