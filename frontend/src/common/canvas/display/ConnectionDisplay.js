import {fabric} from "fabric";
import {animate, calcLine} from "../CanvasUtils";

export default class ConnectionDisplay {

    canvas;
    schedule;
    connectionIcon;
    connectionData;
    fromDisplay;
    toDisplay;

    constructor(canvas, schedule, connectionData, fromDisplay, toDisplay) {
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
    }

    appear() {
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
        this.schedule.terminate();
    }


}