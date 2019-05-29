import Service from "./Service";

const PASSWORD = "password";
const HINT = "hint";

export default class ServiceTex extends Service {

    get password() {
        return this.service.data[PASSWORD];
    }

    get hint() {
        return this.service.data[HINT];
    }

    savePassword(value) {
        super._save(PASSWORD, value );
    }

    saveHint(value) {
        super._save(HINT, value );
    }

}