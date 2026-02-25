import {Layer} from "./Layer"
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux"
import {StatusLightOption} from "../../../standalone/widget/statusLight/StatusLightReducers";
import {editorSiteId} from "../../../editor/EditorRoot";
import {webSocketConnection} from "../../server/WebSocketConnection";

export class LayerStatusLight extends Layer {

    switchLabel: string
    currentOption: number
    options: StatusLightOption[]

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.switchLabel = layer.switchLabel!
        this.currentOption = layer.currentOption!
        this.options = layer.options!
    }

    saveSwitchLabel(value: string) {
        super._save("SWITCH_LABEL", value )
    }

    saveCurrentOption(value: string) {
        super._save("CURRENT_OPTION", value )
    }

    saveTextFor(index: number, value: string) {
        super._save(`TEXT_FOR_${index}`, value )
    }

    saveColorFor(index: number, value: string) {
        super._save(`COLOR_FOR_${index}`, value )
    }

    addOption() {
        const payload = {siteId: editorSiteId, nodeId: this.node.id, layerId: this.layer.id}
        webSocketConnection.send("/editor/addStatusLightOption", payload)
    }

    deleteOption(index: number) {
        const payload = {siteId: editorSiteId, nodeId: this.node.id, layerId: this.layer.id, optionIndex: index}
        webSocketConnection.send("/editor/deleteStatusLightOption", payload)
    }

}
