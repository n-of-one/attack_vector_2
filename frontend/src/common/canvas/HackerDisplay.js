import {fabric} from "fabric";
import {animate, calcLine } from "./CanvasUtils";

const APPEAR_TIME = 20;
const DISAPPEAR_TIME = 10;

export default class HackerDisplay {

    canvas = null;
    startNode = null;

    hackerIcon = null;
    hackerHider = null;
    lineIcon = null;
    labelIcon = null;

    thread = null;
    hacker = null;
    you = false;
    startNodeDisplay = null;

    line = null;
    y = null;
    x = null;

    constructor(canvas, thread, startNodeDisplay, hacker, offset, you) {
        this.canvas = canvas;
        this.thread = thread;
        this.hacker = hacker;
        this.startNodeDisplay = startNodeDisplay;

        const image = document.getElementById(hacker.icon);

        this.x = offset;
        this.y = 810 - 20;

        const size = you ? 60: 40;

        this.hackerIcon = new fabric.Image(image, {
            left: this.x,
            top: this.y,
            height: size,
            width: size,
            opacity: 0,
            selectable: false,
            hoverCursor: "default",
        });
        this.canvas.add(this.hackerIcon);
        animate(this.canvas, this.hackerIcon, "opacity", 1, APPEAR_TIME);

        this.hackerHider = new fabric.Rect({
            left: this.x,
            top: this.y+25,
            height: 35,
            width: 40,
            opacity: 1,
            fill: "#333333",
            selectable: false,
            hoverCursor: "default",
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
            opacity: 0,
            selectable: false,
            hoverCursor: "default",
        });
        this.canvas.add(this.labelIcon);
        animate(this.canvas, this.labelIcon, "opacity", 1, APPEAR_TIME);

        const lineData = calcLine(this, startNodeDisplay);

        this.lineIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#bb8",
                strokeWidth: 2,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });
        this.canvas.add(this.lineIcon);
        this.canvas.sendToBack(this.lineIcon);
        this.thread.run(3, () => animate(this.canvas, this.lineIcon, "opacity", 0.5, 40));
    }

    size() {
        return 30;
    }

    move(newX) {
        this.thread.run(APPEAR_TIME, () => {
            this.x = newX;
            animate(this.canvas, this.hackerIcon, "left", newX, APPEAR_TIME);
            animate(this.canvas, this.hackerHider, "left", newX, APPEAR_TIME);
            animate(this.canvas, this.labelIcon, "left", newX, APPEAR_TIME);
            animate(this.canvas, this.hackerHider, "left", newX, APPEAR_TIME);
            const lineData = calcLine(this, this.startNodeDisplay);
            animate(this.canvas, this.lineIcon, null, lineData.asCoordinates(), APPEAR_TIME);
        });
    }

    disappear() {
        this.thread.run(DISAPPEAR_TIME, () => {
            animate(this.canvas, this.hackerIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.lineIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.labelIcon, "opacity", 0, DISAPPEAR_TIME);
        });
        this.thread.run(0, () => {
            this.canvas.remove(this.hackerIcon);
            this.canvas.remove(this.lineIcon);
            this.canvas.remove(this.labelIcon);
            this.canvas.remove(this.hackerHider);
        });
    }
}
