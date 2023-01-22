import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {LayerOsPanel} from "./type/panel/LayerOsPanel";
import {findElementById} from "../../../../../common/Immutable";
import {ICE_PASSWORD, ICE_TANGLE, OS, TEXT, TIMER_TRIGGER} from "../../../../../common/enums/LayerTypes";
import {LayerTextPanel} from "./type/panel/LayerTextPanel";
import SilentLink from "../../../../../common/component/SilentLink";
import {SELECT_LAYER} from "../../../../EditorActions";
import Glyphicon from "../../../../../common/component/Glyphicon";
import {LayerIcePasswordPanel} from "./type/panel/LayerIcePasswordPanel";
import {LayerIceTanglePanel} from "./type/panel/LayerIceTanglePanel";
import {LayerTimerTriggerPanel} from "./type/panel/LayerTimerTriggerPanel";
import {EditorState} from "../../../../EditorRootReducer";
import {EditorLayerDetails, Node} from "../../../../reducer/NodesReducer";

/* eslint jsx-a11y/anchor-is-valid: 0*/


const renderLayer = (node: Node, layer: EditorLayerDetails) => {

    switch (layer.type) {
        case null:
            return null;
        case OS:
            return <LayerOsPanel node={node} layer={layer}/>;
        case TEXT:
            return <LayerTextPanel node={node} layer={layer}/>;
        case TIMER_TRIGGER:
            return <LayerTimerTriggerPanel node={node} layer={layer}/>;
        case ICE_PASSWORD:
            return <LayerIcePasswordPanel node={node} layer={layer}/>;
        case ICE_TANGLE:
            return <LayerIceTanglePanel node={node} layer={layer}/>;
        default:
            return <div className="text">NodeDetailPanel: ERROR: layer type unknown: {layer.type} for {layer.id}</div>
    }
};


const renderTab = (layer: EditorLayerDetails, currentLayer: EditorLayerDetails, selectLayer: (layer: EditorLayerDetails) => void) => {
    const activeLinkClassName = (layer === currentLayer) ? "nav-link active" : "nav-link";

    return (
        <li role="presentation" className="nav-item" key={layer.id}>
            <SilentLink classNameInput={activeLinkClassName} onClick={() => selectLayer(layer)} aria-controls="home"
                         data-toggle="tab">
                <Glyphicon type={layer.type} size="18px"/>
            </SilentLink>
        </li>
    );
};

export const NodeDetailsPanel = () => {

    const {node, layers, currentLayer} = useSelector((state: EditorState) => {
        if (state.currentNodeId == null) {
            return {layers: [], currentLayer: null};
        }
        const node: Node = findElementById(state.nodes, state.currentNodeId);
        const layer: EditorLayerDetails = findElementById(node.layers, state.currentLayerId);
        return {
            node: node,
            layers: node.layers,
            currentLayer: layer
        };
    });

    const dispatch = useDispatch()
    const selectLayer = (layer: EditorLayerDetails) => dispatch({type: SELECT_LAYER, layerId: layer.id});


    if (!currentLayer) {
        return <div className="row form-horizontal darkWell layerLayerPanel"/>;
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
    );
}
