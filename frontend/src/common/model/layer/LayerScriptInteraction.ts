import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux";

export class LayerScriptInteraction extends Layer {

    interactionKey: string
    message: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.interactionKey = layer.interactionKey!
        this.message = layer.message!
    }

    saveInteractionKey(value: string) {
        super._save("INTERACTION_KEY", value )
    }

    saveMessage(value: string) {
        super._save("MESSAGE", value )
    }

}
