import {SERVER_SCAN_FULL} from "../model/ScanActions";
import {findElementById, updateArrayById} from "../../../common/Immutable";
import {SERVER_ICE_HACKED} from "../model/HackActions";

const defaultState = {};

export default (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_SCAN_FULL:
            return action.data.site;
        case SERVER_ICE_HACKED:
            return serverIceHacked(action.data, state);
        default:
            return state;
    }
}


const serverIceHacked = (update, site) => {
    const node = findElementById(site.nodes, update.nodeId);
    const oldService = node.services.find((service) => service.id === update.serviceId);
    const newService = {...oldService, hacked: true};
    const newServices = updateArrayById(newService, node.services, update.serviceId);
    const newNode = { ...site.nodes, services: newServices };
    const newNodes = updateArrayById(newNode, site.nodes, update.nodeId);
    const newSite = { ...site, nodes: newNodes };
    return newSite;
};
