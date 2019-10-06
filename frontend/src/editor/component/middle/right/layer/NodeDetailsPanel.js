import React from 'react';
import {connect} from "react-redux";
import LayerOsPanel from "./type/panel/LayerOsPanel";
import {findElementById} from "../../../../../common/Immutable";
import {ICE_PASSWORD, ICE_TANGLE, OS, TEXT, TIMER_TRIGGER} from "../../../../../common/enums/LayerTypes";
import LayerTextPanel from "./type/panel/LayerTextPanel";
import SilentLink from "../../../../../common/component/SilentLink";
import {SELECT_LAYER} from "../../../../EditorActions";
import Glyphicon from "../../../../../common/component/Glyphicon";
import LayerIcePasswordPanel from "./type/panel/LayerIcePasswordPanel";
import LayerIceTanglePanel from "./type/panel/LayerIceTanglePanel";
import LayerTimerTriggerPanel from "./type/panel/LayerTimerTriggerPanel";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        selectLayer: layer => dispatch({type: SELECT_LAYER, layerId: layer.id}),
    };
};

let mapStateToProps = (state) => {
    if (state.currentNodeId == null) {
        return {layers: [], currentLayer: null};
    }
    const node = findElementById(state.nodes, state.currentNodeId);
    const layer = findElementById(node.layers, state.currentLayerId);
    return {
        node: node,
        layers: node.layers,
        currentLayer: layer};
};

const renderLayer = (node, layer) => {

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


const renderTab = (layer, currentLayer, selectLayer) => {
    const activeClassName = (layer === currentLayer) ? "active" : "";

    return (
        <li role="presentation" className={activeClassName} key={layer.id}>
            <SilentLink onClick={() => selectLayer(layer)} aria-controls="home" role="tab" data-toggle="tab">
                <Glyphicon type={layer.type} size="18px" />
            </SilentLink>
        </li>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, layers, currentLayer, selectLayer}) => {
        if (!currentLayer) {
            return  <div className="row form-horizontal darkWell layerLayerPanel" />;
        }
        return (
            <div className="row form-horizontal darkWell layerLayerPanel">
                <div className="row">&nbsp;</div>

                <div className="col-lg-12">
                    <ul className="nav nav-tabs" role="tablist" id="node-layers-tab-list">
                        {layers.map(layer => renderTab(layer, currentLayer, selectLayer))}
                    </ul>
                    <br/>
                    {renderLayer(node, currentLayer)}
                </div>
            </div>
        );
    });
