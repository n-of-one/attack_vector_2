import {fabric} from "fabric";
import {animate, easeLinear} from "../../CanvasUtils";


export default class LineElement {

    canvas = null;
    line = null;


    constructor(lineData, color, canvas) {
        this.canvas = canvas;

        this.line = new fabric.Line(
            lineData.asArray(), {
                stroke: color,
                strokeWidth: 2,
                selectable: false,
                hoverCursor: 'default',
                opacity: 1
            });

        this.canvas.add(this.line);
        this.canvas.sendToBack(this.line);
    }

    extendTo(lineData, time) {
        animate(this.canvas, this.line, null, lineData.asCoordinates(), time, easeLinear);
    }

    disappear(time) {
        animate(this.canvas, this.line, 'opacity', 0, time);
    }

    remove() {
        this.canvas.remove(this.line);
    }

}