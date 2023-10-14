import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

const COUNTDOWN = "countdown"
const SHUTDOWN = "shutdown"

export class LayerTripwire extends Layer {

    countdown: string
    shutdown: string


    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch);

        this.countdown = layer.countdown!;
        this.shutdown = layer.shutdown!;
    }

    saveCountdown(value: string) {
        super._save(COUNTDOWN, value );
    }

    saveShutdown(value: string) {
        super._save(SHUTDOWN, value );
    }
}