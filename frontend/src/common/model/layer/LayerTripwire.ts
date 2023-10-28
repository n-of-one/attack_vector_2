import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

export class LayerTripwire extends Layer {

    countdown: string
    shutdown: string
    coreLayerId: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.countdown = layer.countdown!
        this.shutdown = layer.shutdown!
        this.coreLayerId = layer.coreLayerId!
    }

    saveCountdown(value: string) {
        super._save("COUNTDOWN", value )
    }

    saveShutdown(value: string) {
        super._save("SHUTDOWN", value )
    }

    saveCoreLayerId(value: string) {
        super._save("CORE_LAYER_ID", value )
    }
}
