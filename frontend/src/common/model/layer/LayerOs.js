import {EDIT_NETWORK_ID} from "../../../editor/EditorActions";
import Layer from "./Layer";

const NODE_NAME = "nodeName";

export default class LayerOs extends Layer {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.networkId = node.networkId;
        this.nodeName = layer.nodeName;
    }

    saveNetworkId(value) {
        this.dispatch({type: EDIT_NETWORK_ID, nodeId: this.node.id, value: value});
    };

    saveNodeName(value) {
        super._save(NODE_NAME, value );
    }


}