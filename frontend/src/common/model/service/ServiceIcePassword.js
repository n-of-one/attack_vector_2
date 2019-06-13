import Service from "./Service";

const PASSWORD = "password";
const HINT = "hint";
const HACKED = "hacked";

export default class ServiceIcePassword extends Service {

    constructor(service, node, dispatch) {
        super(service, node, dispatch);

        this.password = service.password;
        this.hint = service.hint;
        this.hacked = service.hacked;
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