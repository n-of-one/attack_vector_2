import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer"
import {Dispatch} from "redux"

const KEY_ICE_ID = "iceId"

export class LayerKeyStore extends Layer {

    iceId: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.iceId = layer.iceId!
    }

    saveIceId(value: string) {
        super._save(KEY_ICE_ID, value )
    }

}