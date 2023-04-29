import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {LayerOsPanel} from "./type/panel/app/LayerOsPanel"
import {findElementById} from "../../../../../common/Immutable"
import {LayerType, NETWALK_ICE, OS, PASSWORD_ICE, TANGLE_ICE, TEXT, TIMER_TRIGGER, WORD_SEARCH_ICE} from "../../../../../common/enums/LayerTypes"
import {LayerTextPanel} from "./type/panel/app/LayerTextPanel"
import {SilentLink} from "../../../../../common/component/SilentLink"
import {Glyphicon} from "../../../../../common/component/Glyphicon"
import {LayerIcePasswordPanel} from "./type/panel/ice/LayerIcePasswordPanel"
import {LayerIceTanglePanel} from "./type/panel/ice/LayerIceTanglePanel"
import {LayerTimerTriggerPanel} from "./type/panel/app/LayerTimerTriggerPanel"
import {EditorState} from "../../../../EditorRootReducer"
import {LayerDetails, NodeI} from "../../../../reducer/NodesReducer"
import {SELECT_LAYER} from "../../../../reducer/CurrentLayerIdReducer"
import {LayerIceWordSearchPanel} from "./type/panel/ice/LayerIceWordSearchPanel";
import {LayerIceNetWalkPanel} from "./type/panel/ice/LayerIceNetwalkPanel";
import {LayerSlowIcePanel} from "./type/panel/ice/LayerSlowIcePanel";

/* eslint jsx-a11y/anchor-is-valid: 0*/


const renderLayer = (node: NodeI, layer: LayerDetails) => {

    switch (layer.type) {
        case null:
            return null
        case OS:
            return <LayerOsPanel node={node} layer={layer}/>
        case TEXT:
            return <LayerTextPanel node={node} layer={layer}/>
        case TIMER_TRIGGER:
            return <LayerTimerTriggerPanel node={node} layer={layer}/>
        case PASSWORD_ICE:
            return <LayerIcePasswordPanel node={node} layer={layer}/>
        case TANGLE_ICE:
            return <LayerIceTanglePanel node={node} layer={layer}/>
        case WORD_SEARCH_ICE:
            return <LayerIceWordSearchPanel node={node} layer={layer} />
        case NETWALK_ICE:
            return <LayerIceNetWalkPanel node={node} layer={layer} />
        case LayerType.SLOW_ICE:
            return <LayerSlowIcePanel node={node} layer={layer}/>

        default:
            return <div className="text">NodeDetailPanel: ERROR: layer type unknown: {layer.type} for {layer.id}</div>
    }
}


const renderTab = (layer: LayerDetails, currentLayer: LayerDetails, selectLayer: (layer: LayerDetails) => void) => {
    const activeLinkClassName = (layer === currentLayer) ? "nav-link active" : "nav-link"

    return (
        <li role="presentation" className="nav-item" key={layer.id}>
            <SilentLink classNameInput={activeLinkClassName} onClick={() => selectLayer(layer)} aria-controls="home"
                         data-toggle="tab">
                <Glyphicon type={layer.type} size="18px"/>
            </SilentLink>
        </li>
    )
}

export const NodeDetailsPanel = () => {

    const {node, layers, currentLayer} = useSelector((state: EditorState) => {
        if (state.currentNodeId == null) {
            return {layers: [], currentLayer: null}
        }
        const node: NodeI = findElementById(state.nodes, state.currentNodeId)
        const layer: LayerDetails = findElementById(node.layers, state.currentLayerId!)
        return {
            node: node,
            layers: node.layers,
            currentLayer: layer
        }
    })

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
