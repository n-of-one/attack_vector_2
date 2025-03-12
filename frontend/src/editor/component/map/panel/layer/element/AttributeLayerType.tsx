import React from 'react'
import {SilentLink} from "../../../../../../common/component/SilentLink"
import {Glyphicon} from "../../../../../../common/component/icon/Glyphicon"
import {OS} from "../../../../../../common/enums/LayerTypes"
import {sendRemoveLayer} from "../../../../../server/EditorServerClient"
import {LayerDetails, NodeI} from "../../../../../../common/sites/SiteModel";

const renderRemove = (layer: LayerDetails, remove: () => void) => {
    if (layer.type === OS) {
        return null
    }
    return (
        <span className="pull-right">
            <SilentLink onClick={() => remove()}>
                <Glyphicon name="glyphicon-trash"/>
            </SilentLink>
        </span>
    )
}

interface Props {
    node: NodeI,
    layer: LayerDetails,
    typeDisplay: string
}

export const AttributeLayerType = ({node, layer, typeDisplay}: Props) => {

    const remove = () => {
        sendRemoveLayer({nodeId: node.id, layerId: layer.id})
    }

    return (
        <div className="row form-group layerFieldTopRow">
            <div className="col-lg-3 control-label layerLabel">Type</div>
            <div className="col-lg-8 noRightPadding">
                <span className="strong layer_text_label text_gold" style={{top: "7px", position: "relative"}}>{typeDisplay}</span>
            </div>
            <div className="col-lg-1 layerHelpColumn">
                <span>{renderRemove(layer, remove)}</span>
            </div>
        </div>
    )
}
