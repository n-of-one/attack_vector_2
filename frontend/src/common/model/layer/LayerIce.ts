import Layer from "./Layer";
import {EditorLayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

const STRENGTH = "strength";

export default class LayerIce extends Layer {

    strength: string

    constructor(layer: EditorLayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch);

        this.strength = layer.strength!;
    }

    saveStrength(value: string) {
        super._save(STRENGTH, value );
    }
}
