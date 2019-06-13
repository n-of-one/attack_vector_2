import {EDIT_SERVICE_DATA} from "../../../editor/EditorActions";

const NOTE = "note";
const NAME = "name";

export default class ServiceOs {

    constructor(service, node, dispatch) {
        this.service = service;
        this.node = node;
        this.dispatch = dispatch;
        this.id = service.id;
        this.name = service.name;
        this.note = service.note;
    }


    _save(key, value) {
        this.dispatch({type: EDIT_SERVICE_DATA, nodeId: this.node.id, serviceId: this.service.id, key: key, value: value});
    }

    saveNote(value) {
        this._save(NOTE, value );
    }

    saveName(value) {
        this._save(NAME, value );
    }

}