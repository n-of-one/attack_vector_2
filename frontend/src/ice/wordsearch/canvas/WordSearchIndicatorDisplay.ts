import {fabric} from "fabric";
import {Canvas, Line} from "fabric/fabric-impl";

export class WordSearchIndicatorDisplay {

    line: Line
    canvas: Canvas

    x1: number
    y1: number

    constructor(canvas: Canvas, x: number, y: number) {
        this.canvas = canvas;

        this.x1 = x
        this.y1 = y

        this.line = new fabric.Line(
            [x,y,x,y], {
                stroke: "yellow",
                strokeWidth: 3,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0.8,
            });
        this.logPosition(x, y)
        this.canvas.add(this.line)
        this.canvas.renderAll()
    }

    moved(x2: number, y2: number) {
        const newCoordinates = { x1: this.x1, y1: this.y1, x2, y2 }
        this.line.set(newCoordinates)
        this.line.setCoords()
        this.canvas.renderAll()

        // this.logPosition(x2, y2)

    }

    remove() {
        this.canvas.remove(this.line)
    }

    logPosition(x: number, y: number) {
        console.log(`[ ${this.x1}, ${this.y1}, ${x}, ${y} ]`)
    }
}
