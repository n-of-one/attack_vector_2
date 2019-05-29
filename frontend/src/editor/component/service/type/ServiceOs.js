import {EDIT_NETWORK_ID} from "../../../EditorActions";
import Service from "./Service";

const NODE_NAME = "nodeName";

export default class ServiceOs extends Service {

    get networkId() {
        return this.node.networkId;
    }

    get nodeName() {
        return this.service.data[NODE_NAME];
    }

    saveNetworkId(value) {
        this.dispatch({type: EDIT_NETWORK_ID, nodeId: this.node.id, value: value});
    };

    saveNodeName(value) {
        super._save(NODE_NAME, value );
    }


}