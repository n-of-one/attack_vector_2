import {fabric} from "fabric";
import {animate, calcLine} from "./CanvasUtils";

export default class HackerDisplay {

    canvas = null;
    startNode = null;
    hackerIcon = null;
    thread = null;

    line = null;
    y = null;
    x = null;

    constructor(canvas, thread, startNode, iconThread) {
        this.canvas = canvas;
        this.thread = thread;

        const image = document.getElementById("SCORPION");

        this.x = 607 / 2;
        this.y = 810;

        this.hackerIcon = new fabric.Image(image, {
            left: this.x,
            top: this.y,
            height: 40,
            width: 40,
            opacity: 1,

        });

        this.canvas.add(this.hackerIcon);

        const lineData = calcLine(this, startNode, 20, 20);

        const lineIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#bb8",
                strokeWidth: 2,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });
        this.canvas.add(lineIcon);
        this.canvas.sendToBack(lineIcon);
        this.thread.run(3, () => animate(this.canvas, lineIcon, "opacity", 0.5, 100));
    }
}