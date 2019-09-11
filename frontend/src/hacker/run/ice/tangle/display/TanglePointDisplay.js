import {fabric} from "fabric";

export default class TanglePointDisplay {


    constructor(canvas, pointData) {
        this.canvas = canvas;
        this.id = pointData.id;
        this.lines = [];

        this.icon = new fabric.Circle({
            radius: 6,
            top: pointData.y,
            left: pointData.x,
            fill: "#337ab7",
            stroke: "#000",
            strokeWidth: 2,
            selectable: true,
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: false,
            lockMovementY: false,
            hoverCursor: "auto",
            moveCursor: "auto",
            hasBorders: false,
            display: this,
        });

        this.icon.setControlsVisibility({
            tl: false,
            tr: false,
            br: false,
            bl: false,
            ml: false,
            mt: false,
            mr: false,
            mb: false,
            mtr: false,
        });
    }

    show() {
        this.canvas.add(this.icon);
    }

    addLine(tangleLine) {
        this.lines.push(tangleLine);
    }

    highLight() {
        this.icon.set("fill", "#eee");
        this.lines.forEach(line => line.highlightOtherEnd(this));
    }

    secondaryHighlight() {
        this.icon.set("fill", "#f7ecb5");
    }

    unHighlight() {
        this.secondaryUnHighlight();
        this.lines.forEach(line => line.unHighlightOtherEnd(this));
    }

    secondaryUnHighlight() {
        this.icon.set("fill", "#337ab7");
    }

    moved(x, y) {
        this.icon.setTop(y);
        this.icon.setLeft(x);
        this.icon.setCoords();
        this.updateLines();
    }

    updateLines() {
        this.lines.forEach(line => line.moved())
    }

}