import {fabric} from "fabric";

export interface Display {

    x: number
    y: number
    size : number
    terminate: () => void
    getAllIcons: () => fabric.Object[]
}