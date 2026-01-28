import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {animate} from "../CanvasUtils";
import {delayTicks} from "../../util/Util";

export interface Display {

    x: number
    y: number
    size : number
    terminate: () => void
    getAllIcons: () => fabric.Object[]
}

export class Graphics {

    canvas: Canvas

    constructor(canvas: Canvas) {
        this.canvas = canvas
    }

    fade(durationTicks: number, opacity: number, target: fabric.Object | null ) {
        if (target)
            animate(this.canvas, target, "opacity", opacity, durationTicks)
    }

    fadeOut(durationTicks: number, target: fabric.Object) {
        this.fade(durationTicks, 0, target)
    }

    fadeOutAndRemove(durationTicks: number, target: fabric.Object) {
        this.fadeOut(durationTicks, target)
        delayTicks(10, () => {
            // this.canvas.remove(this.startLineIcon)
            this.canvas.remove(target)
        })
    }

}