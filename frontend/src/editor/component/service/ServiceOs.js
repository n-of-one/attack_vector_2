import {EDIT_NETWORK_ID, EDIT_SERVICE_DATA} from "../../EditorActions";

const KEY_NAME = "name";
const KEY_GM_NOTE = "gmNote";

export default class ServiceOs {

    constructor(service, node, dispatch) {
        this.service = service;
        this.node = node;
        this.dispatch = dispatch;
    }

    get id() {
        return this.service.id;
    }

    get networkId() {
        return this.node.networkId;
    }

    get gmNote() {
        return this.service.data[KEY_GM_NOTE];
    }

    get name() {
        return this.service.data[KEY_NAME];
    }

    _save(key, value) {
        this.dispatch({type: EDIT_SERVICE_DATA, nodeId: this.node.id, serviceId: this.service.id, key: key, value: value});
    }

    saveNetworkId(value) {
        this.dispatch({type: EDIT_NETWORK_ID, nodeId: this.node.id, value: value});
    };

    saveName(value) {
        this._save(KEY_NAME, value );
    }

    saveGmNote(value) {
        this._save(KEY_GM_NOTE, value );
    }


}