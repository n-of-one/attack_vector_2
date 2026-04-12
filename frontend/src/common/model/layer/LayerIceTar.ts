import {LayerIce} from "./LayerIce";
import {Dispatch} from "redux";
import {LayerDetails, NodeI} from "../../sites/SiteModel";

export class LayerIceTar extends LayerIce {

    totalUnits: number

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.totalUnits = layer.totalUnits!
    }
}
