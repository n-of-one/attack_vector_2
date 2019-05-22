import {fabric} from "fabric";
import {animate} from "./CanvasUtils";

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

        const line = new fabric.Line(
            [this.hackerIcon.left, this.hackerIcon.top, startNode.x, startNode.y], {
                stroke: "#bb8",
                strokeWidth: 2,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });
        this.canvas.add(line);
        this.canvas.sendToBack(line);
        this.thread.run(3, () => animate(this.canvas, line, "opacity", 0.5, 100));


    }
}