import React from 'react';
import LayerInfo from "./layer/LayerInfo";


function findProtectedLayer(layers) {
    for (let i = layers.length - 1; i >= 0; i--) {
        const layer = layers[i];
        if (layer.ice && !layer.hacked ) {
            return i;
        }
    }
    return -1;
}

const NodeScanInfoLayers = ({node}) => {

    const layers = node.layers;
    const rendered = [];
    const protectedLayer = findProtectedLayer(layers);

    rendered.push(<span key="_0">Layer Layer<br/></span>);

    for (let i = 0; i < layers.length; i++) {
        const layer = layers[i];
        if (i === protectedLayer) {
            rendered.push(<span key="_1"><br/>--- Layers above are protected by ice --- <br/><br/></span>)
        }
        rendered.push(<LayerInfo layer={layer} key={i}/>)
    }
    return rendered;
};

export default NodeScanInfoLayers;
