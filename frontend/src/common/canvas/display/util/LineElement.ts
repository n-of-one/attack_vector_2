import {fabric} from "fabric";
import {animate, easeLinear, LinePositions} from "../../CanvasUtils";
import {Canvas} from "fabric/fabric-impl";
import {delay} from "../../../Util";

export class LineElement {

    canvas: Canvas
    line: fabric.Line


    constructor(lineData: LinePositions, color: string, canvas: Canvas, styling: any = {}) {
        this.canvas = canvas;

        this.line = new fabric.Line(
            lineData.asArray(), {
                stroke: color,
                strokeWidth: 2,
                selectable: false,
                hoverCursor: 'default',
                // opacity: 1,
                ...styling
            });

        this.canvas.add(this.line);
        delay(() => {
            this.canvas.sendToBack(this.line).renderAll()
        })
    }

    getIcon(): fabric.Line {
        return this.line
    }

    setColor(value: string) {
        this.line.set('stroke', value);
    }

    appear(ticks: number) {
        animate(this.canvas, this.line, "opacity", 1, ticks);
    }

    extendTo(lineData: LinePositions, time: number, ease = easeLinear) {
        animate(this.canvas, this.line, null, lineData.asCoordinates(), time, ease);
    }

    disappear(ticks: number) {
        animate(this.canvas, this.line, 'opacity', 0, ticks);
    }

    remove() {
        this.canvas.remove(this.line);
    }


}