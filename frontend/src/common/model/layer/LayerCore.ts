import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux"

export class LayerCore extends Layer {

    revealNetwork: boolean

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.revealNetwork = layer.revealNetwork!
    }

    saveRevealNetwork(value: string) {
        super._save("REVEAL_NETWORK", value)
    }
}
