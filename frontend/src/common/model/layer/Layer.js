import {sendEditLayerData} from "../../../editor/server/EditorServerClient";

const NOTE = "note";
const NAME = "name";

export default class Layer {

    constructor(layer, node, dispatch) {
        this.layer = layer;
        this.node = node;
        this.dispatch = dispatch;
        this.id = layer.id;
        this.name = layer.name;
        this.note = layer.note;
    }


    _save(key, value) {
        sendEditLayerData( {nodeId: this.node.id, layerId: this.layer.id, key: key, value: value})
    }

    saveNote(value) {
        this._save(NOTE, value );
    }

    saveName(value) {
        this._save(NAME, value );
    }

}