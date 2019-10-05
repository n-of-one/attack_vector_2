import {fabric} from "fabric";
import {animate, calcLine, calcLineStart} from "../CanvasUtils";
import Schedule from "../../Schedule";
import {COLOR_PATROLLER_LINE, SIZE_NORMAL} from "./util/DisplayConstants";
import LineElement from "./util/LineElement";


export default class SnifferLeashDisplay {

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

    // arriveAt(nodeDisplay) {
    //     if (nodeDisplay === this.targetHackerDisplay.currentNodeDisplay) {
    //         this.attemptToCatch(nodeDisplay.id);
    //     }
    //     else {
    //         this.extendLeash();
    //     }
    // }
    //
    // attemptToCatch(nodeId) {
    //     this.dispatch({type: SNIFFER_LEASH_ARRIVE_HACKER, hackerId: this.targetHackerId, nodeId: nodeId})
    // }
    //
    // extendLeash() {
    //     // this.nextNodeDisplay = findNextNodeDisplay();
    // }
    //
    //

    capture(hackerId) {
        this.displayById[hackerId].capturedByLeash();
    }

    disappear() {
        this.schedule.run(0, () => {
            animate(this.canvas, this.patrollerIcon, "opacity", 0, 20);
        });
    }

    //
    //
    // scheduleMoveStep(nextDisplay, currentDisplay) {
    //     this.schedule.run(22, () => this.moveStep(nextDisplay, currentDisplay, 20));
    // }
    //
    // moveStep(nextDisplay, currentDisplay, time) {
    //     const lineIcon = this.createProbeLine(currentDisplay, nextDisplay);
    //     this.lineIcons.push(lineIcon);
    //     const lineData = calcLine(currentDisplay, nextDisplay, this.padding);
    //     animate(this.canvas, lineIcon, null, lineData.asCoordinates(), time, easeLinear);
    // }
    //
    // createProbeLine(currentDisplay, nextDisplay) {
    //     const lineData = calcLineStart(currentDisplay, nextDisplay, 22, this.padding);
    //
    //     const lineIcon = new fabric.Line(
    //         lineData.asArray(), {
    //             stroke: "#285ba0",
    //             strokeWidth: 2,
    //             selectable: false,
    //             hoverCursor: 'default',
    //             opacity: 1
    //         });
    //
    //     this.canvas.add(lineIcon);
    //     this.canvas.sendToBack(lineIcon);
    //
    //     return lineIcon;
    // }
    //
    // processProbeArrive(scanType, nodeId, currentDisplay) {
    //     this.probeIcon.setLeft(currentDisplay.x + 5);
    //     this.probeIcon.setTop(currentDisplay.y + 5);
    //
    //     switch (scanType) {
    //         case SCAN_NODE_INITIAL:
    //             return this.scanInside(nodeId, SCAN_NODE_INITIAL);
    //         case SCAN_CONNECTIONS:
    //             return this.scanOutside(nodeId);
    //         case SCAN_NODE_DEEP:
    //             return this.scanInside(nodeId, SCAN_NODE_DEEP);
    //         default:
    //             return this.probeError(scanType, nodeId);
    //     }
    // }
    //
    // scanInside(nodeId, action) {
    //     this.schedule.run(50, () => {
    //         console.time("scan");
    //         this.animateZoom(SIZE_SMALL, 50);
    //         this.animateOpacity(0.9, 50);
    //
    //     });
    //     this.schedule.run(25, () => {
    //         this.animateZoom(SIZE_SMALL_MEDIUM, 25);
    //         this.animateOpacity(0, 35);
    //     });
    //     const finishMethod = () => {
    //         if (this.yourProbe) {
    //             this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: action});
    //         }
    //     };
    //     this.finishProbe(finishMethod);
    // }
    //
    // scanOutside(nodeId) {
    //     this.schedule.run(50, () => {
    //         console.time("scan");
    //         this.animateZoom(SIZE_LARGE, 50);
    //         this.animateOpacity(0.7, 50);
    //     });
    //     this.schedule.run(25, () => {
    //         this.animateZoom(SIZE_MEDIUM_LARGE, 35);
    //         this.animateOpacity(0, 35);
    //     });
    //     const finishMethod = () => {
    //         if (this.yourProbe) {
    //             this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: SCAN_CONNECTIONS});
    //         }
    //     };
    //     this.finishProbe(finishMethod)
    // }
    //
    // finishProbe(finishMethod) {
    //     this.schedule.run(10, () => {
    //         this.lineIcons.forEach(lineIcon => {
    //             animate(this.canvas, lineIcon, 'opacity', 0, 10);
    //         });
    //         finishMethod();
    //     });
    //     this.schedule.run(0, () => {
    //         this.canvas.remove(this.probeIcon);
    //         this.lineIcons.forEach(lineIcon => {
    //             this.canvas.remove(lineIcon);
    //         });
    //         console.timeEnd("scan");
    //         this.performAutoScan();
    //     });
    // }
    //
    // performAutoScan() {
    //     if (this.autoScan && this.yourProbe) {
    //         this.dispatch({type: AUTO_SCAN});
    //     }
    // }
    //
    // probeError(scanType, nodeId) {
    //     this.schedule.run(30, () => {
    //         animate(this.canvas, this.probeIcon, "height", 0, 30)
    //     });
    //     this.schedule.run(0, () => {
    //         this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + scanType + "[/] unknown. nodeId: [info]" + nodeId})
    //     });
    // }
    //
    // remove() {
    //     this.canvas.remove(this.probeIcon);
    //     this.lineIcons.forEach(lineIcon => {
    //         this.canvas.remove(lineIcon);
    //     });
    // }
    //
    // animateZoom(size, time) {
    //     animate(this.canvas, this.probeIcon, 'width', size, time);
    //     animate(this.canvas, this.probeIcon, 'height', size, time);
    // }
    // animateOpacity(opacity, time) {
    //     animate(this.canvas, this.probeIcon, 'opacity', opacity, time);
    // }
    //
    // terminate() {
    //     this.schedule.terminate();
    // }
    //

}