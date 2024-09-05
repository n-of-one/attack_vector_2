import {findElementById, updateArrayById} from "../../../common/util/Immutable";
import {SiteProperties, sitePropertiesDefault} from "../../../editor/reducer/SitePropertiesReducer";
import {Connection} from "../../../editor/reducer/ConnectionsReducer";
import {siteStateDefault, SiteStateI} from "../../../editor/reducer/SiteStateReducer";
import {NodeI} from "../../../editor/reducer/NodesReducer";
import {AnyAction} from "redux";
import {SERVER_ENTERED_RUN, SERVER_LAYER_HACKED} from "../../RunServerActionProcessor";


export interface Site {
    id: string,
    siteProperties: SiteProperties,
    nodes: NodeI[],
    connections: Connection[],
    state: SiteStateI,
    startNodeId: string,
    nodeStatuses: NodeStatus[],
    layerStatuses: LayerStatus[]
}

export interface NodeStatus {
    id: string,
    nodeId: string,
    runId: string,
    hacked: boolean
}

export interface LayerStatus {
    id: string,
    layerId: string,
    runId: string,
    hacked: boolean,
    hackedBy: string[]
}

const defaultState = {
    id: "",
    siteProperties: sitePropertiesDefault,
    nodes: [],
    connections: [],
    state: siteStateDefault,
    startNodeId: "",
    nodeStatuses: [],
    layerStatuses: []
};

export const siteReducer = (state: Site = defaultState, action: AnyAction) => {
    switch (action.type) {
        case SERVER_ENTERED_RUN:
            return action.data.site;
        case SERVER_LAYER_HACKED:
            return serverIceHacked(action.data, state);
        default:
            return state;
    }
};

interface IceHackedData {
    layerId: string,
    nodeId: string
}

const serverIceHacked = (update: IceHackedData, site: Site) => {
    const node: NodeI = findElementById(site.nodes, update.nodeId);
    const oldLayer = node.layers.find((layer) => layer.id === update.layerId);
    const newLayer = {...oldLayer, hacked: true};
    const newLayers = updateArrayById(newLayer, node.layers, update.layerId);
    const newNode = { ...site.nodes, layers: newLayers };
    const newNodes = updateArrayById(newNode, site.nodes, update.nodeId);
    const newSite = { ...site, nodes: newNodes };
    return newSite;
};
