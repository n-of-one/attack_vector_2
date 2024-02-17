import {fabric} from "fabric";
import {Canvas, Circle} from "fabric/fabric-impl";
import {TanglePoint} from "../reducer/TangleIceReducer";
import {TangleLineDisplay} from "./TangleLineDisplay";

const aCOLOR_NORMAL = "#5ec6c8"
const aCOLOR_HIGHLIGHT_PRIMARY = "#337ab7"
const aCOLOR_HIGHLIGHT_SECONDARY = "#337ab7"

const COLOR_NORMAL = "#5ec6c8"
const COLOR_STROKE_NORMAL = "#000"
const STROKE_WIDTH_NORMAL = 0.7

const STROKE_WIDTH_HIGHLIGHT = 2

const COLOR_HIGHLIGHT_PRIMARY = "#5ec6c8"
const COLOR_STROKE_HIGHLIGHT_PRIMARY = "#337ab7"

const COLOR_HIGHLIGHT_SECONDARY = "#111"
const COLOR_STROKE_HIGHLIGHT_SECONDARY = "#337ab7"

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
            fill: COLOR_NORMAL,
            stroke: COLOR_STROKE_NORMAL,
            strokeWidth: STROKE_WIDTH_NORMAL,
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
        this.icon.set("fill", COLOR_HIGHLIGHT_PRIMARY);
        this.icon.set("stroke", COLOR_STROKE_HIGHLIGHT_PRIMARY);
        this.icon.set("strokeWidth", STROKE_WIDTH_HIGHLIGHT);
        this.lines.forEach(line => line.highlightOtherEnd(this));
    }

    secondaryHighlight() {
        this.icon.set("fill", COLOR_HIGHLIGHT_SECONDARY);
        this.icon.set("strokeWidth", STROKE_WIDTH_HIGHLIGHT);
        this.icon.set("stroke", COLOR_STROKE_HIGHLIGHT_SECONDARY);
    }

    unHighlight() {
        this.secondaryUnHighlight();
        this.lines.forEach(line => line.unHighlightOtherEnd(this));
    }

    secondaryUnHighlight() {
        this.icon.set("fill", COLOR_NORMAL);
        this.icon.set("stroke", COLOR_STROKE_NORMAL);
        this.icon.set("strokeWidth", STROKE_WIDTH_NORMAL);

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