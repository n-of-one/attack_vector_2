import {fabric} from "fabric";
import {animate, calcLine} from "./CanvasUtils";

export default class HackerDisplay {

    canvas = null;
    startNode = null;
    hackerIcon = null;
    labelIcon = null;
    thread = null;
    hacker = null;
    you = false;

    line = null;
    y = null;
    x = null;

    constructor(canvas, thread, startNodeDisplay, hacker, offset, you) {
        this.canvas = canvas;
        this.thread = thread;
        this.hacker = hacker;

        const image = document.getElementById(hacker.icon);

        this.x = offset;
        this.y = 810 - 20;

        const size = you ? 60: 40;

        this.hackerIcon = new fabric.Image(image, {
            left: this.x,
            top: this.y,
            height: size,
            width: size,
            opacity: 1,

        });
        this.canvas.add(this.hackerIcon);


        this.hackerHider = new fabric.Rect({
            left: this.x,
            top: this.y+25,
            height: 35,
            width: 40,
            opacity: 1,
            fill: "#333333"
        });
        this.canvas.add(this.hackerHider);


        const hackerName = you ? "You" : hacker.userName;
        this.labelIcon = new fabric.Text(hackerName, {
            fill: "#f0ad4e",    // color-ok
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            // fontWeight: 10,
            left: this.x,
            top: this.y + 15,
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