import Layer from "./Layer";
import {editorCanvas} from "../../../editor/component/middle/middle/EditorCanvas";
import {sendEditNetworkId} from "../../../editor/server/EditorServerClient";
import {EditorLayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

const NODE_NAME = "nodeName";

export class LayerOs extends Layer {

    networkId: string
    nodeName: string

    constructor(layer: EditorLayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch);

        this.networkId = node.networkId
        this.nodeName = layer.nodeName!
    }

    saveNetworkId(value: string) {
        sendEditNetworkId({nodeId: this.node.id, value})
        editorCanvas.updateNetworkId({nodeId: this.node.id, value})
    };

    saveNodeName(value: string) {
        super._save(NODE_NAME, value );
    }
}
