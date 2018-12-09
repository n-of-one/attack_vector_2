import {SERVER_SITE_FULL, SERVER_ADD_NODE, SERVER_MOVE_NODE} from "../EditorActions";
import { assertNotNullUndef } from "../../common/Assert";

export default (state = [], action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.nodes;
        case SERVER_ADD_NODE: return addNode(action.data, state);
        case SERVER_MOVE_NODE: return moveNode(action.data, state);
        default: return state;
    }
}

let addNode = (data, nodeList) => {
    let node = {
        id: data.id,
        x: data.x,
        y: data.y,
        type: data.type,
        connections: [ ],
        services: data.services,
        ice: data.ice,
    };
    return [ ...nodeList, node ];
};

let moveNode = (data, nodeList) => {
  let nodeIndex = findNodeIndex(nodeList, data.nodeId);
  let oldNode = nodeList[nodeIndex];
  let newNode = { ...oldNode, x: data.x, y: data.y};
  let newNodeList = [ ...nodeList ];
  newNodeList.splice(nodeIndex, 1, newNode);
  return newNodeList;
};

let findNodeIndex = (nodeList, id) => {
    let nodeIndex = null;
    nodeList.forEach( (node, index) => {
        if (node.id === id) {
            nodeIndex = index;
        }
    });
    assertNotNullUndef(nodeIndex, {nodeList, id});
    return nodeIndex;
};
