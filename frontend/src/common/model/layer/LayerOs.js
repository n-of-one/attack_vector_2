import Layer from "./Layer";
import {editorCanvas} from "../../../editor/component/middle/middle/EditorCanvas";
import {sendEditNetworkId} from "../../../editor/server/EditorServerClient";

const NODE_NAME = "nodeName";

export class LayerOs extends Layer {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.networkId = node.networkId;
        this.nodeName = layer.nodeName;
    }

    saveNetworkId(value) {
        sendEditNetworkId({nodeId: this.node.id, value})
        editorCanvas.updateNetworkId({nodeId: this.node.id, value})
    };

    saveNodeName(value) {
        super._save(NODE_NAME, value );
    }
}
