import Service from "./Service";

const KEY_TEXT = "text";

export default class ServiceTex extends Service {

    constructor(service, node, dispatch) {
        super(service, node, dispatch);

        this.text = service.text;
    }

    saveText(value) {
        super._save(KEY_TEXT, value );
    }
}