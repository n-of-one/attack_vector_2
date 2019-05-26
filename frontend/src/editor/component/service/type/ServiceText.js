import Service from "./Service";

const KEY_NAME = "name";
const KEY_TEXT = "text";

export default class ServiceTex extends Service {

    get name() {
        return this.service.data[KEY_NAME];
    }

    get text() {
        return this.service.data[KEY_TEXT];
    }

    saveName(value) {
        super._save(KEY_NAME, value );
    }

    saveText(value) {
        super._save(KEY_TEXT, value );
    }
}