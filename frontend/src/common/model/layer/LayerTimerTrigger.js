import Layer from "./Layer";

const MINUTES = "minutes";
const SECONDS = "seconds";

export default class LayerTimerTrigger extends Layer {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.minutes = layer.minutes;
        this.seconds = layer.seconds;
    }

    saveMinutes(value) {
        super._save(MINUTES, value );
    }

    saveSeconds(value) {
        super._save(SECONDS, value );
    }
}