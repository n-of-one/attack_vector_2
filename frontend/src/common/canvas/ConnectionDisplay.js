import {fabric} from "fabric";
import {animate, calcLine} from "./CanvasUtils";

export default class ConnectionDisplay {

    canvas = null;
    thread = null;

    id = null;
    fromIcon = null;
    toIcon = null;

    connectionIcon = null;

    constructor(canvas, thread, id, fromIcon, toIcon) {
        this.canvas = canvas;
        this.thread = thread;

        this.id = id;
        this.fromIcon = fromIcon;
        this.toIcon = toIcon;

        const lineData = calcLine(fromIcon, toIcon, 22, 22);

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
        this.thread.run(3, () => {
            animate(this.canvas, this.connectionIcon, "opacity", 0.5, 40);
        });
    }
}