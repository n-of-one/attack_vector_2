import {fabric} from "fabric";
import {animate, calcLine, calcLineStart, easeInSine, easeLinear} from "../CanvasUtils";
import Schedule from "../../Schedule";
import {COLOR_PATROLLER_LINE, SIZE_NORMAL} from "./util/DisplayConstants";
import LineElement from "./util/LineElement";


export default class SnifferPatrollerDisplay {

    canvas = null;
    dispatch = null;
    displayById = null;

    patrollerId = null;
    currentNodeId = null;

    schedule = null;

    lineElements = [];

    constructor({patrollerId, nodeId, appearTicks}, canvas, dispatch, displayById) {
        this.patrollerId = patrollerId;
        this.currentNodeId = nodeId;

        this.canvas = canvas;
        this.schedule = new Schedule();
        this.dispatch = dispatch;
        this.displayById = displayById;


        this.currentNodeDisplay = displayById[nodeId];

        const image = document.getElementById("PATROLLER_3");

        this.patrollerIcon = new fabric.Image(image, {
            left: this.currentNodeDisplay.x,
            top: this.currentNodeDisplay.y,
            height: SIZE_NORMAL,
            width: SIZE_NORMAL,
            opacity: 0,
            selectable: false,
        });
        this.canvas.add(this.patrollerIcon);
        this.canvas.bringToFront(this.patrollerIcon);

        animate(this.canvas, this.patrollerIcon, "opacity", 1, appearTicks);
        this.schedule.wait(appearTicks);
    }

    move(fromNodeId, toNodeId, moveTicks) {

        this.schedule.run(moveTicks, () => {
            const fromNodeDisplay = this.displayById[fromNodeId];
            const toNodeDisplay = this.displayById[toNodeId];

            const lineStartData = calcLineStart(fromNodeDisplay, toNodeDisplay, 22, 4);
            const lineElement = new LineElement(lineStartData, COLOR_PATROLLER_LINE, this.canvas);

            this.lineElements.push(lineElement);

            const lineEndData = calcLine(fromNodeDisplay, toNodeDisplay, 4);
            lineElement.extendTo(lineEndData, moveTicks);
        });

    }


    capture(hackerId) {
        this.displayById[hackerId].capturedByLeash(this);
    }

    captureComplete() {
        // this.canvas.bringToFront(this.patrollerIcon);
        // animate(this.canvas, this.patrollerIcon, 'width', 160, 100, easeInSine);
        // animate(this.canvas, this.patrollerIcon, 'height', 160, 100, easeInSine);
    }

    disappear() {
        this.schedule.run(0, () => {
            animate(this.canvas, this.patrollerIcon, "opacity", 0, 20);
        });
    }



}