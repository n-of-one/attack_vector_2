import React from 'react'
import {LayerInfo} from "./LayerInfo"
import {LayerDetails, NodeI} from "../../../../editor/reducer/NodesReducer"

function findProtectedLayer(layers: LayerDetails[]) {
    for (let i = layers.length - 1; i >= 0; i--) {
        const layer = layers[i]
        if (layer.ice && !layer.hacked) {
            return i
        }
    }
    return -1
}


export const NodeScanInfoLayers = ({node, allLayersRevealed}: { node: NodeI, allLayersRevealed: boolean }) => {

    const layers = node.layers
    const rendered = []
    const protectedLayer = findProtectedLayer(layers)

    rendered.push(<span key="_0">Level Layer<br/></span>)

    for (let i = 0; i < layers.length; i++) {
        const layer = layers[i]
        if (i === protectedLayer) {
            rendered.push(<span key="_1"><br/>--- Layers above are protected by ice --- <br/><br/></span>)
        }
        const layerRevealed = (i >= protectedLayer) || allLayersRevealed
        rendered.push(<LayerInfo layer={layer} key={i} revealed={layerRevealed}/>)
    }
    return <>{rendered}</>
}