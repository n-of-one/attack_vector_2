import React from 'react'
import SilentLink from "../../../../../common/component/SilentLink"
import Glyphicon from "../../../../../common/component/Glyphicon"
import {OS} from "../../../../../common/enums/LayerTypes"
import {EditorLayerDetails, NodeI} from "../../../../reducer/NodesReducer"
import {sendRemoveLayer} from "../../../../server/EditorServerClient"

const renderRemove = (layer: EditorLayerDetails, remove: () => void) => {
    if (layer.type === OS) {
        return null
    }
    return (
        <span className="pull-right" style={{display: "block"}}>
            <SilentLink onClick={() => remove()}>
                <Glyphicon name="glyphicon-remove" size="18px" display="block"/>
            </SilentLink>
        </span>
    )
}

interface Props {
    node: NodeI,
    layer: EditorLayerDetails,
    typeDisplay: string
}
export const LayerType = ({node, layer, typeDisplay}: Props) => {

    const remove = () => {
        sendRemoveLayer({nodeId: node.id, layerId: layer.id})
    }

    return (
        <div className="row form-group layerFieldTopRow">
            <div className="col-lg-3 control-label layerLabel">Type</div>
            <div className="col-lg-8">
                <div className="strong layer_text_label text_gold d-flex justify-content-between">
                    <span>{typeDisplay}</span>
                    <span>{renderRemove(layer, remove)}</span>
                </div>
            </div>
        </div>
    )
}
