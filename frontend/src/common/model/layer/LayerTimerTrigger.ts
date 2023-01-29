import Layer from "./Layer";
import {EditorLayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

const MINUTES = "minutes";
const SECONDS = "seconds";

export default class LayerTimerTrigger extends Layer {

    minutes: number
    seconds: number

    constructor(layer: EditorLayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch);

        this.minutes = layer.minutes!;
        this.seconds = layer.seconds!;
    }

    saveMinutes(value: string) {
        super._save(MINUTES, value );
    }

    saveSeconds(value: string) {
        super._save(SECONDS, value );
    }
}