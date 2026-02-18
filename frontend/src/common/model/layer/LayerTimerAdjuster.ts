import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux";

export enum TimerAdjustmentType {
    SPEED_UP = "SPEED_UP",
    SLOW_DOWN = "SLOW_DOWN"
}

export enum TimerAdjustmentRecurring {
    FIRST_ENTRY_ONLY = "FIRST_ENTRY_ONLY",
    EVERY_ENTRY = "EVERY_ENTRY",
    EACH_HACKER_ONCE = "EACH_HACKER_ONCE",
}


export class LayerTimerAdjuster extends Layer {

    amount: string
    adjustmentType: TimerAdjustmentType
    recurring: TimerAdjustmentRecurring

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
