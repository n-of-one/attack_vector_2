import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer"
import {Dispatch} from "redux"

const STRENGTH = "strength"

export class LayerIce extends Layer {

    strength: string


    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.strength = layer.strength!
    }

    saveStrength(value: string) {
        super._save(STRENGTH, value )
    }
}
