import {LayerIce} from "./LayerIce";
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer";
import {Dispatch} from "redux";

export class LayerSlowIce extends LayerIce {

    totalUnits: number
    time1Level1Hacker: string
    time1Level5Hacker: string
    time5Level10Hackers: string

    constructor(layer: LayerDetails, node: NodeI, dispatch: Dispatch) {
        super(layer, node, dispatch)

        this.totalUnits = layer.totalUnits!
        this.time1Level1Hacker = layer.time1Level1Hacker!
        this.time1Level5Hacker = layer.time1Level5Hacker!
        this.time5Level10Hackers = layer.time5Level10Hackers!
    }


}