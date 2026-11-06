import {fabric} from "fabric";
import {animate, calcLine} from "../CanvasUtils";
import {Canvas} from "fabric/fabric-impl";
import {Schedule} from "../../util/Schedule";
import {Connection, ConnectionStatus} from "../../../editor/reducer/ConnectionsReducer";
import {NodeDisplay} from "./NodeDisplay";
import {Display} from "./Display";
import {APPEAR_ANIMATION_TICKS} from "./DisplayAnimationConstants";

const STYLE_BY_STATUS = {
    OPEN: {stroke: "#cccccc", strokeDashArray: [3, 3], opacity: 0.5},
    PENDING: {stroke: "#f0ad4e", strokeDashArray: [3, 3], opacity: 0.9},
    SEVERED: {stroke: "#777777", strokeDashArray: [1, 6], opacity: 0.4},
}

export class ConnectionDisplay implements Display {

    canvas: Canvas
    schedule: Schedule | null
    connectionIcon: fabric.Line
    severedIcon: fabric.Text
    fromDisplay: NodeDisplay
    toDisplay: NodeDisplay

    size = 0

    connectionData: Connection
    status: ConnectionStatus
    shownInEditor = false
    x: number
    y: number

    constructor(canvas: Canvas, schedule: Schedule | null , connectionData: Connection, fromDisplay: NodeDisplay, toDisplay: NodeDisplay,
                status: ConnectionStatus = "OPEN") {
        this.canvas = canvas;
        this.schedule = schedule;

        this.connectionData = connectionData;
        this.status = status
        this.fromDisplay = fromDisplay;
        this.toDisplay = toDisplay;

        const lineData = calcLine(fromDisplay, toDisplay);

        this.connectionIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: STYLE_BY_STATUS[status].stroke,
                strokeWidth: 2,
                strokeDashArray: STYLE_BY_STATUS[status].strokeDashArray,
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });

        this.canvas.add(this.connectionIcon);
        this.canvas.sendToBack(this.connectionIcon);

        this.x = (fromDisplay.x + toDisplay.x) / 2
        this.y = (fromDisplay.y + toDisplay.y) / 2

        this.severedIcon = new fabric.Text("✕", {
            fill: STYLE_BY_STATUS.SEVERED.stroke,
            fontFamily: "courier",
            fontSize: 16,
            left: this.x,
            top: this.y,
            opacity: 0,
            selectable: false,
            hoverCursor: 'default',
        })
        this.canvas.add(this.severedIcon)
    }

    getAllIcons() {
        return [this.connectionIcon, this.severedIcon]
    }

    appear() {
        if (!this.schedule) throw Error("schedule not initialized")
        this.schedule.run(3, () => {
            animate(this.canvas, this.connectionIcon, "opacity", this.lineOpacity(), APPEAR_ANIMATION_TICKS);
            animate(this.canvas, this.severedIcon, "opacity", this.severedIconOpacity(), APPEAR_ANIMATION_TICKS);
        });
    }

    updateStatus(status: ConnectionStatus) {
        if (status === this.status) return
        this.status = status
        this.connectionIcon.set({stroke: STYLE_BY_STATUS[status].stroke, strokeDashArray: STYLE_BY_STATUS[status].strokeDashArray})
        animate(this.canvas, this.connectionIcon, "opacity", this.lineOpacity(), APPEAR_ANIMATION_TICKS);
        animate(this.canvas, this.severedIcon, "opacity", this.severedIconOpacity(), APPEAR_ANIMATION_TICKS);
    }

    private lineOpacity() {
        return (this.shownInEditor && this.status === "OPEN") ? 1 : STYLE_BY_STATUS[this.status].opacity
    }

    private severedIconOpacity() {
        return (this.status === "SEVERED") ? 0.8 : 0
    }

    show() {
        this.shownInEditor = true
        this.connectionIcon.opacity = this.lineOpacity();
        this.severedIcon.opacity = this.severedIconOpacity();
    }
    //
    // moveFrom(newFromDisplay) {
    //     this.fromDisplay = newFromDisplay;
    //     this.move();
    // }
    //
    // moveTo(newToDisplay) {
    //     this.toDisplay = newToDisplay;
    //     this.move();
    // }

    endPointsMoved() {
        const lineData = calcLine(this.fromDisplay, this.toDisplay);

        this.connectionIcon.set(lineData.asCoordinates());
        this.connectionIcon.setCoords();

        this.x = (this.fromDisplay.x + this.toDisplay.x) / 2
        this.y = (this.fromDisplay.y + this.toDisplay.y) / 2
        this.severedIcon.set({left: this.x, top: this.y})
    }

    terminate() {
        if (!this.schedule) throw Error("schedule not initialized")
        this.schedule.terminate();
    }


}
