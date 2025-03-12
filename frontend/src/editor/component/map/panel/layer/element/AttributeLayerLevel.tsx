import React from 'react'
import {SilentLink} from "../../../../../../common/component/SilentLink"
import {LayerDetails, NodeI} from "../../../../../../common/sites/SiteModel";
import {sendSwapLayers} from "../../../../../server/EditorServerClient"

/* eslint jsx-a11y/anchor-is-valid: 0*/

interface Props {
    node: NodeI,
    layer: LayerDetails
}

export const AttributeLayerLevel = ({node, layer}: Props) => {

    const swap = (node: NodeI, layer: LayerDetails, level: number) => {
        sendSwapLayers({nodeId: node.id, fromId: layer.id, toId: node.layers[level].id})
    }

    const up = (node: NodeI, layer: LayerDetails) => swap(node, layer, layer.level - 1)
    const down = (node: NodeI, layer: LayerDetails) => swap(node, layer, layer.level + 1)

    const level = layer.level

    let downClickable = (level > 1)
    let downHtml = downClickable ? (
        <SilentLink classNameInput="textLink" onClick={() => up(node, layer)}><>◀</></SilentLink>) : (<span>◀</span>
    )

    let upClickable = (level !== 0 && level < (node.layers.length - 1))
    let upHtml = upClickable ? (
        <SilentLink classNameInput="textLink" onClick={() => down(node, layer)}><>▶</></SilentLink>) : (<span>▶</span>
    )

    return (
        <div className="row form-group layerFieldRow">
            <div className="col-lg-3 layerLabel">Layer</div>
            <div className="col-lg-8">
                <div className="text-muted strong layer_text_label">
                    {level}&nbsp;{downHtml}&nbsp;{upHtml}
                </div>
            </div>
        </div>
    )
}
