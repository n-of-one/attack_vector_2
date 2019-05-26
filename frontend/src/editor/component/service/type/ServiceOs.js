import {EDIT_NETWORK_ID} from "../../../EditorActions";
import Service from "./Service";

const KEY_NAME = "name";

export default class ServiceOs extends Service {

    get networkId() {
        return this.node.networkId;
    }

    get name() {
        return this.service.data[KEY_NAME];
    }

    saveNetworkId(value) {
        super.dispatch({type: EDIT_NETWORK_ID, nodeId: this.node.id, value: value});
    };

    saveName(value) {
        super._save(KEY_NAME, value );
    }


}