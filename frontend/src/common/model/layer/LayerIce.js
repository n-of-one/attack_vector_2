import Layer from "./Layer";

const STRENGTH = "strength";

export default class LayerIce extends Layer {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.strength = layer.strength;
    }

    saveStrength(value) {
        super._save(STRENGTH, value );
    }


}