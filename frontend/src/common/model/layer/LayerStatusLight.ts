import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux"

export class LayerStatusLight extends Layer {

    status: boolean
    textForGreen: string
    textForRed: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.status = layer.status!
        this.textForGreen = layer.textForGreen!
        this.textForRed = layer.textForRed!
    }

    saveStatus(value: string) {
        super._save("STATUS", value )
    }

    saveTextForRed(value: string) {
        super._save("TEXT_FOR_RED", value )
    }

    saveTextForGreen(value: string) {
        super._save("TEXT_FOR_GREEN", value )
    }
}
