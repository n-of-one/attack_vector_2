import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerPanel} from "../LayerPanel"
import {LayerDetails, NodeI} from "../../../../../../../../common/sites/SiteModel";
import {AttributeDropdown} from "../../../element/AttributeDropdown";
import {EditorState} from "../../../../../../../EditorRootReducer";
import {editorCanvas} from "../../../../../canvas/EditorCanvas";
import {SELECT_LAYER} from "../../../../../../../reducer/CurrentLayerIdReducer";
import {LayerKeyStore} from "../../../../../../../../common/model/layer/LayerKeystore";
import {iceLayerOptions} from "./LayerUtils";


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
        dispatch({type: SELECT_LAYER, layerId: iceLayerId})
    }
    const navigateIfIceId = (iceLayerId) ? navigateToLayer : undefined

    return (
        <LayerPanel typeDisplay="Keystore" layerObject={keystore}>
            <AttributeDropdown key={key("status")} label="For ICE"
                               value={keystore.iceLayerId }
                               options={options}
                               save={value => keystore.saveIceId(value)}
                               tooltipId="forIce" tooltipText="The ICE for which this keystore contains the password/key"
                               navigate={navigateIfIceId}
            />
        </LayerPanel>
    )
}



