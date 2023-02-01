import {sendEditLayerData} from "../../../editor/server/EditorServerClient";
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

const NOTE = "note";
const NAME = "name";

export default class Layer {

    layer: LayerDetails
    node: NodeI
    dispatch: Dispatch
    id: string
    name: string
    note: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        this.layer = layer;
        this.node = node;
        this.dispatch = dispatch;
        this.id = layer.id;
        this.name = layer.name;
        this.note = layer.note;
    }


    _save(key: string, value: string) {
        sendEditLayerData( {nodeId: this.node.id, layerId: this.layer.id, key: key, value: value})
    }

    saveNote(value: string) {
        this._save(NOTE, value );
    }

    saveName(value: string) {
        this._save(NAME, value );
    }

}