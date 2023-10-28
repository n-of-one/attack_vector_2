import {LayerIce} from "./LayerIce";
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

export class LayerIcePassword extends LayerIce {

    password: string
    hint: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch);

        this.password = layer.password!;
        this.hint = layer.hint!;
    }

    savePassword(value: string) {
        super._save("PASSWORD", value );
    }

    saveHint(value: string) {
        super._save("HINT", value );
    }
}
