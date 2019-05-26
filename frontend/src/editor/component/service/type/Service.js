import {EDIT_SERVICE_DATA} from "../../../EditorActions";

const KEY_NOTE = "note";

export default class ServiceOs {

    constructor(service, node, dispatch) {
        this.service = service;
        this.node = node;
        this.dispatch = dispatch;
    }

    get id() {
        return this.service.id;
    }

    get note() {
        return this.service.data[KEY_NOTE];
    }

    _save(key, value) {
        this.dispatch({type: EDIT_SERVICE_DATA, nodeId: this.node.id, serviceId: this.service.id, key: key, value: value});
    }


    saveNote(value) {
        this._save(KEY_NOTE, value );
    }


}