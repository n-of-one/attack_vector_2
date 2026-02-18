import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux";

export class LayerScriptCredits extends Layer {

    amount: number
    stolen: boolean

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.amount = layer.amount! as number
        this.stolen = layer.stolen!
    }

    saveAmount(value: string) {
        super._save("AMOUNT", value)
    }

    saveStolen(value: string) {
        super._save("STOLEN", value)
    }

}
