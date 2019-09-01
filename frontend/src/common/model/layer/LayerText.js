import Layer from "./Layer";

const KEY_TEXT = "text";

export default class LayerTex extends Layer {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.text = layer.text;
    }

    saveText(value) {
        super._save(KEY_TEXT, value );
    }
}