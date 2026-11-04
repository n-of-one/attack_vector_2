import React from "react";
import {LayerDetails} from "../../../../../common/sites/SiteModel";
import {TimerAdjustmentType} from "../../../../../common/model/layer/LayerTimerAdjuster";
import {TriggerRecurrence} from "../../../../../common/model/layer/TriggerRecurrence";

export const ScanInfoTripwire = ({layer}: {layer: LayerDetails}) => {
    return (
        <> - reset after: <span className="text-warning" >{layer.countdown}</span> and shutdown for <span className="text-warning" >{layer.shutdown}</span></>
    )
}

export const ScanInfoShutdownAccelerator = ({layer}: {layer: LayerDetails}) => {
    const type = layer.adjustmentType?.toString() === TimerAdjustmentType.SPEED_UP.toString() ? "speeds up" : "slows down"

    return (
        <><br/> &nbsp; &nbsp; &nbsp;  - {type} shutdown timers by <span className="text-warning" >{layer.amount}</span><br/>{recurrence(layer.recurring!)}</>
    )
}

const recurrence = (recurring: TriggerRecurrence) => {
    switch (recurring) {
        case TriggerRecurrence.FIRST_ENTRY_ONLY:
            return <> &nbsp; &nbsp; &nbsp; - only triggers once</>
        case TriggerRecurrence.EVERY_ENTRY:
            return <> &nbsp; &nbsp; &nbsp; - triggers on every entry of any hacker</>
        case TriggerRecurrence.EACH_HACKER_ONCE:
            return <> &nbsp; &nbsp; &nbsp; - triggers on the first entry of each hacker</>
        default:
            return <></>
    }
}
