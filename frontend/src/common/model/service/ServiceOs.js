import {EDIT_NETWORK_ID} from "../../../editor/EditorActions";
import Service from "./Service";

const NODE_NAME = "nodeName";

export default class ServiceOs extends Service {

    constructor(service, node, dispatch) {
        super(service, node, dispatch);

        this.networkId = node.networkId;
        this.nodeName = service.nodeName;
    }

    saveNetworkId(value) {
        this.dispatch({type: EDIT_NETWORK_ID, nodeId: this.node.id, value: value});
    };

    saveNodeName(value) {
        super._save(NODE_NAME, value );
    }


}