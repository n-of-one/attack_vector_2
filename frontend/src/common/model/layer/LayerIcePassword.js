import Layer from "./Layer";

const PASSWORD = "password";
const HINT = "hint";
const HACKED = "hacked";

export default class LayerIcePassword extends Layer {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.password = layer.password;
        this.hint = layer.hint;
        this.hacked = layer.hacked;
    }

    savePassword(value) {
        super._save(PASSWORD, value );
    }

    saveHint(value) {
        super._save(HINT, value );
    }

    saveHacked(value) {
        super._save(HACKED, value );
    }

}