import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerOsPanel} from "./type/panel/app/LayerOsPanel"
import {findElementById} from "../../../../../common/util/Immutable"
import {LayerType, NETWALK_ICE, OS, PASSWORD_ICE, TANGLE_ICE, TEXT, WORD_SEARCH_ICE} from "../../../../../common/enums/LayerTypes"
import {LayerTextPanel} from "./type/panel/app/LayerTextPanel"
import {SilentLink} from "../../../../../common/component/SilentLink"
import {LayerIcePasswordPanel} from "./type/panel/ice/LayerIcePasswordPanel"
import {LayerIceTanglePanel} from "./type/panel/ice/LayerIceTanglePanel"
import {EditorState} from "../../../../EditorRootReducer"
import {LayerDetails, NodeI} from "../../../../reducer/NodesReducer"
import {SELECT_LAYER} from "../../../../reducer/CurrentLayerIdReducer"
import {LayerIceWordSearchPanel} from "./type/panel/ice/LayerIceWordSearchPanel";
import {LayerIceNetWalkPanel} from "./type/panel/ice/LayerIceNetwalkPanel";
import {LayerIceTarPanel} from "./type/panel/ice/LayerIceTarPanel";
import {LayerStatusLightPanel} from "./type/panel/app/LayerStatusLightPanel";
import {Icon} from "../../../../../common/component/icon/Icon";
import {createSelector} from "@reduxjs/toolkit";
import {LayerKeyStorePanel} from "./type/panel/app/LayerKeyStorePanel";
import {LayerTripwirePanel} from "./type/panel/app/LayerTripwirePanel";
import {LayerCorePanel} from "./type/panel/app/LayerCorePanel";

/* eslint jsx-a11y/anchor-is-valid: 0*/


const renderLayer = (node: NodeI, layer: LayerDetails) => {

    switch (layer.type) {
        case null:
            return null
        case OS:
            return <LayerOsPanel node={node} layer={layer}/>
        case TEXT:
            return <LayerTextPanel node={node} layer={layer}/>
        case PASSWORD_ICE:
            return <LayerIcePasswordPanel node={node} layer={layer}/>
        case TANGLE_ICE:
            return <LayerIceTanglePanel node={node} layer={layer}/>
        case WORD_SEARCH_ICE:
            return <LayerIceWordSearchPanel node={node} layer={layer} />
        case NETWALK_ICE:
            return <LayerIceNetWalkPanel node={node} layer={layer} />
        case LayerType.TAR_ICE:
            return <LayerIceTarPanel node={node} layer={layer}/>
        case LayerType.STATUS_LIGHT:
            return <LayerStatusLightPanel node={node} layer={layer}/>
        case LayerType.LOCK:
            return <LayerStatusLightPanel node={node} layer={layer}/>
        case LayerType.KEYSTORE:
            return <LayerKeyStorePanel node={node} layer={layer}/>
        case LayerType.TRIPWIRE:
            return <LayerTripwirePanel node={node} layer={layer}/>
        case LayerType.CORE:
            return <LayerCorePanel node={node} layer={layer}/>

        default:
            return <div className="text">NodeDetailsPanel: ERROR: layer type unknown: {layer.type} for {layer.id}</div>
    }
}


const renderTab = (layer: LayerDetails, currentLayer: LayerDetails, selectLayer: (layer: LayerDetails) => void) => {
    const activeLinkClassName = (layer === currentLayer) ? "nav-link active" : "nav-link"
    const svgColor = (layer === currentLayer) ? "white" : "navlinkblue"

    return (
        <li role="presentation" className="nav-item" key={layer.id}>
            <SilentLink classNameInput={activeLinkClassName} onClick={() => selectLayer(layer)} aria-controls="home"
                         data-toggle="tab">
                <Icon type={layer.type} svgColor={svgColor}/>
            </SilentLink>
        </li>
    )
}

const selectCurrentNodeId = (state: EditorState) => state.currentNodeId
const selectCurrentLayerId = (state: EditorState) => state.currentLayerId
const selectNodes = (state: EditorState) => state.nodes
const selectNodeDetails = createSelector(selectCurrentNodeId, selectCurrentLayerId, selectNodes, (currentNodeId, currentLayerId, nodes) => {
    if (currentNodeId == null) {
        return {layers: [], currentLayer: null}
    }
    const node: NodeI = findElementById(nodes, currentNodeId)
    const layer: LayerDetails = findElementById(node.layers, currentLayerId!)
    return {
        node: node,
        layers: node.layers,
        currentLayer: layer
    }
})

export const NodeDetailsPanel = () => {

    const {node, layers, currentLayer} = useSelector(selectNodeDetails)

    const dispatch = useDispatch()
    const selectLayer = (layer: LayerDetails) => dispatch({type: SELECT_LAYER, layerId: layer.id})


    if (!currentLayer) {
        return <div className="row form-horizontal darkWell layerLayerPanel"/>
    }

    return (
        <div className="row darkWell layerLayerPanel">
            <div className="col-lg-12" style={{marginTop: "20px"}}>

                <ul className="nav nav-tabs" role="tablist" id="node-layers-tab-list" style={{marginLeft: "7px"}}>
                    {layers.map(layer => renderTab(layer, currentLayer, selectLayer))}
                </ul>
                <br/>
                {renderLayer(node, currentLayer)}
            </div>
        </div>
    )
}
