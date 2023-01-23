import LayerIce from "./LayerIce";

const PASSWORD = "password";
const HINT = "hint";

export class LayerIcePassword extends LayerIce {

    constructor(layer, node, dispatch) {
        super(layer, node, dispatch);

        this.password = layer.password;
        this.hint = layer.hint;
    }

    savePassword(value) {
        super._save(PASSWORD, value );
    }

    saveHint(value) {
        super._save(HINT, value );
    }
}
