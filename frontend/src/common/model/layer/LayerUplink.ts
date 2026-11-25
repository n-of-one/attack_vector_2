import {Layer} from "./Layer";
import {LayerDetails, NodeI} from "../../sites/SiteModel";
import {Dispatch} from "redux";

export class LayerUplink extends Layer {

    targetSiteId: string
    targetSiteName: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.targetSiteId = layer.targetSiteId ?? ""
        this.targetSiteName = layer.targetSiteName ?? this.targetSiteId
    }

    saveTargetSiteId(value: string) {
        super._save("TARGET_SITE_ID", value)
    }
}
