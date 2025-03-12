import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux"

export class LayerText extends Layer {

    text: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.text = layer.text!
    }

    saveText(value: string) {
        super._save("TEXT", value )
    }
}
