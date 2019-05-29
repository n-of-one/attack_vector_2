import Service from "./Service";

const KEY_TEXT = "text";

export default class ServiceTex extends Service {

    get text() {
        return this.service.data[KEY_TEXT];
    }

    saveText(value) {
        super._save(KEY_TEXT, value );
    }
}