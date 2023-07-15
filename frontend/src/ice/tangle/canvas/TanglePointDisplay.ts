import {fabric} from "fabric";
import {Canvas, Circle} from "fabric/fabric-impl";
import {TanglePoint} from "../reducer/TangleIceReducer";
import {TangleLineDisplay} from "./TangleLineDisplay";

export class TanglePointDisplay {

    canvas: Canvas
    id: string
    lines: TangleLineDisplay[]
    icon: Circle

    constructor(canvas: Canvas, pointData: TanglePoint) {
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
        });
        (this.icon as any).display = this

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

        // this.idIcon = new fabric.Text("" + pointData.id, {
        //     fill: "#000",
        //     fontFamily: "SpaceMono",
        //     fontSize: 12,
        //     top: pointData.y + 10,
        //     left: pointData.x + 10,
        //     selectable: false,
        // })
    }

    show() {
        this.canvas.add(this.icon);
        // this.canvas.add(this.idIcon);
    }

    addLine(tangleLine: TangleLineDisplay) {
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

    moved(x: number, y: number) {
        this.icon.top = y;
        this.icon.left = x;
        this.icon.setCoords();

        // this.idIcon.setTop(y+10);
        // this.idIcon.setLeft(x+10);
        // this.idIcon.setCoords();

        this.updateLines();
    }

    updateLines() {
        this.lines.forEach(line => line.moved())
    }

}