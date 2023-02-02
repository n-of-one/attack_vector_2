import {fabric} from "fabric";
import {animate, calcLine} from "../CanvasUtils";
import {Canvas} from "fabric/fabric-impl";
import {Schedule} from "../../Schedule";
import {Connection} from "../../../editor/reducer/ConnectionsReducer";
import {NodeDisplay} from "./NodeDisplay";
import {Display} from "./Display";

export class ConnectionDisplay implements Display {

    canvas: Canvas
    schedule: Schedule | null
    connectionIcon: fabric.Line
    fromDisplay: NodeDisplay
    toDisplay: NodeDisplay

    size = 0

    connectionData: Connection
    x: number
    y: number

    constructor(canvas: Canvas, schedule: Schedule | null , connectionData: Connection, fromDisplay: NodeDisplay, toDisplay: NodeDisplay) {
        this.canvas = canvas;
        this.schedule = schedule;

        this.connectionData = connectionData;
        this.fromDisplay = fromDisplay;
        this.toDisplay = toDisplay;

        const lineData = calcLine(fromDisplay, toDisplay);

        this.connectionIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#cccccc",
                strokeWidth: 2,
                strokeDashArray: [3, 3],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });

        this.canvas.add(this.connectionIcon);
        this.canvas.sendToBack(this.connectionIcon);

        this.x = (fromDisplay.x + toDisplay.x) / 2
        this.y = (fromDisplay.y + toDisplay.y) / 2
    }

    getAllIcons() {
        return [this.connectionIcon]
    }

    appear() {
        if (!this.schedule) throw Error("schedule not initialized")
        this.schedule.run(3, () => {
            animate(this.canvas, this.connectionIcon, "opacity", 0.5, 40);
        });
    }

    show() {
        this.connectionIcon.opacity = 1;
    }
    //
    // moveFrom(newFromDisplay) {
    //     this.fromDisplay = newFromDisplay;
    //     this.move();
    // }
    //
    // moveTo(newToDisplay) {
    //     this.toDisplay = newToDisplay;
    //     this.move();
    // }

    endPointsMoved() {
        const lineData = calcLine(this.fromDisplay, this.toDisplay);

        this.connectionIcon.set(lineData.asCoordinates());
        this.connectionIcon.setCoords();
    }

    terminate() {
        if (!this.schedule) throw Error("schedule not initialized")
        this.schedule.terminate();
    }


}