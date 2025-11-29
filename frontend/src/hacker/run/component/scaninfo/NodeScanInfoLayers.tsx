import React from 'react'
import {LayerInfo} from "./LayerInfo"
import {LayerDetails, NodeI} from "../../../../common/sites/SiteModel";

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

    if (node.layers[0].nodeName) {
        rendered.push(<span key="_name">Name: {node.layers[0].nodeName}<br/><br/></span>)
    }

    rendered.push(<span key="_0">Level Layer<br/></span>)

    for (let i = 0; i < layers.length; i++) {
        const layer = layers[i]
        if (i === protectedLayer) {
            rendered.push(<span key="_1"><br/>--- Layers above are protected by ICE --- <br/><br/></span>)
        }
        const layerRevealed = (i >= protectedLayer) || allLayersRevealed
        rendered.push(<LayerInfo layer={layer} key={i} revealed={layerRevealed}/>)
    }
    return <>{rendered}</>
}
