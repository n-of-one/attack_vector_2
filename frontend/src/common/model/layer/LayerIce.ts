import {Layer} from "./Layer"
import {Dispatch} from "redux"
import {LayerDetails, NodeI} from "../../sites/SiteModel";

export class LayerIce extends Layer {

    strength: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.strength = layer.strength!
    }

    saveStrength(value: string) {
        super._save("STRENGTH", value )
    }
}
