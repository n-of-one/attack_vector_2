import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer"
import {Dispatch} from "redux"

const KEY_STATUS = "status"
const KEY_TEXT_FOR_RED = "textForRed"
const KEY_TEXT_FOR_GREEN = "textForGreen"

export class LayerStatusLight extends Layer {

    appId: string
    status: boolean
    textForGreen: string
    textForRed: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.appId = layer.appId!
        this.status = layer.status!
        this.textForGreen = layer.textForGreen!
        this.textForRed = layer.textForRed!
    }

    saveStatus(value: string) {
        super._save(KEY_STATUS, value )
    }

    saveTextForRed(value: string) {
        super._save(KEY_TEXT_FOR_RED, value )
    }

    saveTextForGreen(value: string) {
        super._save(KEY_TEXT_FOR_GREEN, value )
    }

}