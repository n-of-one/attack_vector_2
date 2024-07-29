import {LayerIce} from "./LayerIce";
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

export class LayerIceTangle extends LayerIce {

    clusters: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        const clusterCount = (layer.clusters) ? layer.clusters : 1
        this.clusters = clusterCount.toString()
    }

    saveClusters(value: string) {
        super._save("CLUSTERS", value )
    }

}
