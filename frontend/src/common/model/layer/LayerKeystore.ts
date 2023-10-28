import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer"
import {Dispatch} from "redux"

export class LayerKeyStore extends Layer {

    iceLayerId: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.iceLayerId = layer.iceLayerId!
    }

    saveIceId(value: string) {
        super._save("ICE_LAYER_ID", value )
    }
}
