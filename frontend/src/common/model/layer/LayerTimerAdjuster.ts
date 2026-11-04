import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux";
import {TriggerRecurrence} from "./TriggerRecurrence";

export enum TimerAdjustmentType {
    SPEED_UP = "SPEED_UP",
    SLOW_DOWN = "SLOW_DOWN"
}


export class LayerTimerAdjuster extends Layer {

    amount: string
    adjustmentType: TimerAdjustmentType
    recurring: TriggerRecurrence

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.amount = layer.amount! as string
        this.adjustmentType = layer.adjustmentType!
        this.recurring = layer.recurring !
    }

    saveAmount(value: string) {
        super._save("AMOUNT", value)
    }

    saveAdjustmentType(value: string) {
        super._save("ADJUSTMENT_TYPE", value)
    }

    saveAdjustmentRecurring(value: string) {
        super._save("ADJUSTMENT_RECURRING", value)
    }

}
