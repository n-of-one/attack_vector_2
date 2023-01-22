import {
    SERVER_ADD_NODE,
    SERVER_ADD_LAYER,
    SERVER_MOVE_NODE,
    SERVER_NODE_UPDATED,
    SERVER_SITE_FULL,
    SERVER_UPDATE_NETWORK_ID,
    SERVER_UPDATE_LAYER
} from "../EditorActions";
import {findElementById, updateArrayById} from "../../common/Immutable";
import {AnyAction} from "redux";

export enum NodeType {
    TRANSIT_1 = "TRANSIT_1",
    TRANSIT_2 = "TRANSIT_2",
    TRANSIT_3 = "TRANSIT_3",
    TRANSIT_4 = "TRANSIT_4",
    SYSCON = "SYSCON",
    DATA_STORE = "DATA_STORE",
    PASSCODE_STORE = "PASSCODE_STORE",
    RESOURCE_STORE = "RESOURCE_STORE",
    ICE_1 = "ICE_1",
    ICE_2 = "ICE_2",
    ICE_3 = "ICE_3",
    UNHACKABLE = "UNHACKABLE",
    MANUAL_1 = "MANUAL_1",
    MANUAL_2 = "MANUAL_2",
    MANUAL_3 = "MANUAL_3",
}

export enum LayerType {
    OS= "OS",
    TEXT= "TEXT",
    TIMER_TRIGGER= "TIMER_TRIGGER",
    ICE_PASSWORD= "ICE_PASSWORD",
    ICE_TANGLE= "ICE_TANGLE",
}

export interface EditorLayerDetails {
    id: string
    type: LayerType
    level: number
    name: string
    note: string
}

export interface Node {
    id: string,
    siteId: string,
    type: NodeType,
    x: number,
    y: number,
    ice: boolean,
    layers: Array<EditorLayerDetails>,
    networkId: string
}

const NodesReducer = (state: Array<Node> = [], action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.nodes;
        case SERVER_ADD_NODE:
            return addNode(action.data, state);
        case SERVER_MOVE_NODE:
            return moveNode(action.data, state);
        case SERVER_UPDATE_NETWORK_ID:
            return serverUpdateNetworkId(action.data, state);
        case SERVER_UPDATE_LAYER :
            return serverUpdateLayer(action.data, state);
        case SERVER_ADD_LAYER :
            return serverAddLayer(action.data, state);
        case SERVER_NODE_UPDATED :
            return serverRemoveLayer(action.data.node, state);

        default:
            return state;
    }
};

const addNode = (data: Node, nodeList: Array<Node>): Array<Node> => {
    const node = {...data, connections: []};
    return [...nodeList, node];
};



const moveNode = (data: {nodeId: string, x: number, y: number}, nodeList: Array<Node>) => {
    const newNodeData = {x: data.x, y: data.y};
    return updateArrayById(newNodeData, nodeList, data.nodeId);
};


const serverUpdateLayer = (update: {nodeId: string, layerId: string, layer: EditorLayerDetails}, nodes: Array<Node>) => {
    const node = findElementById(nodes, update.nodeId);
    const newLayers = updateArrayById(update.layer, node.layers, update.layerId);

    const newNodeLayers = {layers: newLayers};

    const newNodes = updateArrayById(newNodeLayers, nodes, update.nodeId);
    return newNodes;
};


const serverUpdateNetworkId = (update: {nodeId: string,  networkId: string}, nodes: Array<Node>) => {
    const newNodeData = {networkId: update.networkId};
    const newNodes = updateArrayById(newNodeData, nodes, update.nodeId);
    return newNodes;
};

const serverAddLayer = (data: {nodeId: string, layer: EditorLayerDetails}, nodes: Array<Node>) => {
    const node = findElementById(nodes, data.nodeId);
    const layer = data.layer;
    const newLayers = [...node.layers, layer];

    const newNodeLayers = {layers: newLayers};

    const newNodes = updateArrayById(newNodeLayers, nodes, data.nodeId);
    return newNodes;

};

const serverRemoveLayer = (node: Node, nodes: Array<Node>) => {
    const newNodes = updateArrayById(node, nodes, node.id);
    return newNodes;
};


export {NodesReducer}