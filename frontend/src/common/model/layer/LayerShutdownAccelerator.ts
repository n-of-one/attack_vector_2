import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux";

export class LayerShutdownAccelerator extends Layer {

    increase: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.increase = layer.increase!
    }

    saveIncrease(value: string) {
        super._save("INCREASE", value )
    }
}
