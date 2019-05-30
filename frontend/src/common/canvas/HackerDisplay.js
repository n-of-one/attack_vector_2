import {fabric} from "fabric";
import {animate, calcLine} from "./CanvasUtils";

export default class HackerDisplay {

    canvas = null;
    startNode = null;
    hackerIcon = null;
    labelIcon = null;
    thread = null;
    hacker = null;

    line = null;
    y = null;
    x = null;

    constructor(canvas, thread, startNodeDisplay, hacker) {
        this.canvas = canvas;
        this.thread = thread;
        this.hacker = hacker;

        // const image = document.getElementById(icon);
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

        this.labelIcon = new fabric.Text(hacker.userName, {
            fill: "#f0ad4e",    // color-ok
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            // fontWeight: 10,
            left: this.x,
            top: this.y - 30,
            textAlign: "center", // "center", "right" or "justify".
            opacity: 1,
            selectable: false,
        });
        this.canvas.add(this.labelIcon);

        const lineData = calcLine(this, startNodeDisplay);

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

    size() {
        return 30;
    }
}