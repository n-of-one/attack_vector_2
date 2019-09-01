import {SERVER_SCAN_FULL} from "../model/ScanActions";
import {findElementById, updateArrayById} from "../../../common/Immutable";
import {SERVER_LAYER_HACKED} from "../model/HackActions";

const defaultState = {};

export default (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_SCAN_FULL:
            return action.data.site;
        case SERVER_LAYER_HACKED:
            return serverIceHacked(action.data, state);
        default:
            return state;
    }
}


const serverIceHacked = (update, site) => {
    const node = findElementById(site.nodes, update.nodeId);
    const oldLayer = node.layers.find((layer) => layer.id === update.layerId);
    const newLayer = {...oldLayer, hacked: true};
    const newLayers = updateArrayById(newLayer, node.layers, update.layerId);
    const newNode = { ...site.nodes, layers: newLayers };
    const newNodes = updateArrayById(newNode, site.nodes, update.nodeId);
    const newSite = { ...site, nodes: newNodes };
    return newSite;
};
