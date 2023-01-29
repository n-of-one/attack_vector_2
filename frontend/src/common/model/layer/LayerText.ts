import Layer from "./Layer";
import {EditorLayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

const KEY_TEXT = "text";

export default class LayerText extends Layer {

    text: string

    constructor(layer: EditorLayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch);

        this.text = layer.text!;
    }

    saveText(value: string) {
        super._save(KEY_TEXT, value );
    }
}