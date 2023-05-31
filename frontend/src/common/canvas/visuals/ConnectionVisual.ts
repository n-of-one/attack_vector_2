import {fabric} from "fabric";
import {animate, easeLinear, LinePositions} from "../CanvasUtils";
import {Canvas} from "fabric/fabric-impl";
import {delay, delayTicks} from "../../util/Util";

export class ConnectionVisual {

    canvas: Canvas
    line: fabric.Line


    constructor(lineData: LinePositions, color: string, canvas: Canvas, styling: any = {}) {
        this.canvas = canvas;

        this.line = new fabric.Line(
            lineData.asArray(), {
                stroke: color,
                strokeWidth: 2,
                selectable: true,
                hoverCursor: 'default',
                // opacity: 1,
                ...styling
            });

        this.canvas.add(this.line);
        this.canvas.sendToBack(this.line).renderAll()
        delay(() => {
            this.line.selectable = false
        })
    }

    getIcon(): fabric.Line {
        return this.line
    }

    setColor(value: string) {
        this.line.set('stroke', value);
    }

    appear(durationTicks: number) {
        animate(this.canvas, this.line, "opacity", 1, durationTicks);
    }

    extendTo(lineData: LinePositions, durationTicks: number, ease = easeLinear) {
        animate(this.canvas, this.line, null, lineData.asCoordinates(), durationTicks, ease);
    }

    disappear(durationTicks: number) {
        animate(this.canvas, this.line, 'opacity', 0, durationTicks);
        delayTicks(durationTicks, () => {
            this.remove()
        })
    }

    remove() {
        this.canvas.remove(this.line);
    }


}