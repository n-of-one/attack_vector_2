import {fabric} from "fabric";
import {animate, easeLinear} from "../../CanvasUtils";
import {TICK_MILLIS} from "../../../Schedule";


export default class LineElement {

    canvas = null;
    line = null;


    constructor(lineData, color, canvas, styling) {
        this.canvas = canvas;

        this.line = new fabric.Line(
            lineData.asArray(), {
                stroke: color,
                strokeWidth: 2,
                selectable: false,
                hoverCursor: 'default',
                opacity: 1,
                ...styling
            });

        this.canvas.add(this.line);
        this.canvas.sendToBack(this.line);
    }

    setColor(value) {
        this.line.set('stroke', value);
    }

    extendTo(lineData, time, ease = easeLinear) {
        animate(this.canvas, this.line, null, lineData.asCoordinates(), time, ease);
    }

    disappear(ticks) {
        animate(this.canvas, this.line, 'opacity', 0, ticks);
    }

    remove() {
        this.canvas.remove(this.line);
    }

    disappearAndRemove(ticks) {
        this.disappear(ticks);
        setTimeout( () => this.remove,ticks * TICK_MILLIS);
    }

}