import {fabric} from "fabric";
import {Canvas, Circle} from "fabric/fabric-impl";
import {TanglePoint} from "../reducer/TangleIceReducer";
import {TangleLineDisplay} from "./TangleLineDisplay";


const COLOR_NORMAL_1 = "#5ec6c8"
const COLOR_NORMAL_2 = "#C8B65E"
const COLOR_NORMAL_3 = "#6CC85E"
const COLOR_NORMAL_4 = "#9C7ECA"
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
    colorNormal: string
    cluster: number

    constructor(canvas: Canvas, pointData: TanglePoint, clustersRevealed: boolean) {
        this.canvas = canvas;
        this.id = pointData.id;
        this.cluster = pointData.cluster;
        this.lines = [];

        this.colorNormal = this.determineColorNormal(clustersRevealed)

        this.icon = new fabric.Circle({
            radius: 6,
            top: pointData.y,
            left: pointData.x,
            fill: this.colorNormal,
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

    determineColorNormal(clustersRevealed: boolean): string {
        if (!clustersRevealed) {
            return COLOR_NORMAL_1
        }
        switch (this.cluster) {
            case 1:
                return COLOR_NORMAL_1
            case 2:
                return COLOR_NORMAL_2
            case 3:
                return COLOR_NORMAL_3
            case 4:
                return COLOR_NORMAL_4
            default:
                return COLOR_NORMAL_1
        }
    }

    show() {
        this.canvas.add(this.icon);
        // this.canvas.add(this.idIcon);
    }

    addLine(tangleLine: TangleLineDisplay) {
        this.lines.push(tangleLine);
    }

    revealClusters() {
        this.colorNormal = this.determineColorNormal(true)
        this.icon.set("fill", this.colorNormal);
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
        this.icon.set("fill", this.colorNormal);
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
