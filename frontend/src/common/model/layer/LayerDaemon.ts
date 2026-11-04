import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../sites/SiteModel"
import {Dispatch} from "redux"
import {TriggerRecurrence} from "./TriggerRecurrence"

export class LayerDaemon extends Layer {

    text: string
    requiredTrait: string
    recurring: TriggerRecurrence

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.text = layer.text!
        this.requiredTrait = layer.requiredTrait ?? ''
        this.recurring = layer.recurring!
    }

    saveText(value: string) {
        super._save('TEXT', value)
    }

    saveRequiredTrait(value: string) {
        super._save('REQUIRED_TRAIT', value)
    }

    saveRecurring(value: string) {
        super._save('RECURRING', value)
    }
}
