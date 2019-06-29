import {fabric} from "fabric";
import {animate, calcLine, calcLineStart, easeLinear} from "./CanvasUtils";
import {AUTO_SCAN, PROBE_SCAN_NODE} from "../../hacker/run/model/ScanActions";
import {SCAN_CONNECTIONS, SCAN_NODE_DEEP, SCAN_NODE_INITIAL} from "../../hacker/run/model/NodeScanTypes";
import {TERMINAL_RECEIVE} from "../terminal/TerminalActions";

const SIZE_SMALL = 20;
const SIZE_SMALL_MEDIUM = 30;
const SIZE_MEDIUM = 40;
const SIZE_LARGE = 80;
const SIZE_MEDIUM_LARGE = 60;

export default class ConnectionDisplay {

    canvas = null;
    dispatch = null;

    id = null;
    schedule = null;
    autoScan = null;
    yourProbe = null;
    probeIcon = null;

    lineIcons = [];

    constructor(canvas, schedule, dispatch, {path, scanType, autoScan}, hackerDisplay, yourProbe, displayById) {
        this.canvas = canvas;
        this.schedule = schedule;
        this.autoScan = autoScan;
        this.yourProbe = yourProbe;
        this.dispatch = dispatch;
        this.padding = yourProbe ? 3: 5;


        const imageNumber = Math.floor(Math.random() * 10) + 1;
        const image = document.getElementById("PROBE_" + imageNumber);

        this.probeIcon = new fabric.Image(image, {
            left: hackerDisplay.x,
            top: hackerDisplay.y,
            height: 40,
            width: 40,
            opacity: 0,
            selectable: false,
        });

        this.canvas.add(this.probeIcon);
        this.canvas.bringToFront(this.probeIcon);

        // animate(this.canvas, this.probeIcon, "opacity", 0.4, 40);


        let currentDisplay = hackerDisplay;
        path.forEach((nodeId) => {
            const nextDisplay = displayById[nodeId];
            this.scheduleMoveStep(nextDisplay, currentDisplay);
            currentDisplay = nextDisplay;
        });
        const lastNodeId = path.pop();
        schedule.run(0, () => {
            this.processProbeArrive(scanType, lastNodeId, currentDisplay);
        });
    }

    scheduleMoveStep(nextDisplay, currentDisplay) {
        this.schedule.run(22, () => this.moveStep(nextDisplay, currentDisplay, 20));
    }

    moveStep(nextDisplay, currentDisplay, time) {
        const lineIcon = this.createProbeLine(currentDisplay, nextDisplay);
        this.lineIcons.push(lineIcon);
        const lineData = calcLine(currentDisplay, nextDisplay, this.padding);
        animate(this.canvas, lineIcon, null, lineData.asCoordinates(), time, easeLinear);
    }

    createProbeLine(currentDisplay, nextDisplay) {
        const lineData = calcLineStart(currentDisplay, nextDisplay, 22, this.padding);

        const lineIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#285ba0",
                strokeWidth: 2,
                selectable: false,
                hoverCursor: 'default',
                opacity: 1
            });

        this.canvas.add(lineIcon);
        this.canvas.sendToBack(lineIcon);

        return lineIcon;
    }

    processProbeArrive(scanType, nodeId, currentDisplay) {
        this.probeIcon.setLeft(currentDisplay.x + 5);
        this.probeIcon.setTop(currentDisplay.y + 5);

        // animate(this.canvas, this.probeIcon, 'left', nextDisplay.x + leftDelta, time, easeLinear);
        // animate(this.canvas, this.probeIcon, 'top', nextDisplay.y + topDelta, time, easeLinear);

        switch (scanType) {
            case SCAN_NODE_INITIAL:
                return this.scanInside(nodeId, SCAN_NODE_INITIAL);
            case SCAN_CONNECTIONS:
                return this.scanOutside(nodeId);
            case SCAN_NODE_DEEP:
                return this.scanInside(nodeId, SCAN_NODE_DEEP);
            default:
                return this.probeError(scanType, nodeId);
        }
    }

    scanInside(nodeId, action) {
        this.schedule.run(50, () => {
            this.animateZoomAndOpacity(SIZE_SMALL, 0.8, 50);
        });
        this.schedule.run(25, () => {
            this.animateZoomAndOpacity(SIZE_SMALL_MEDIUM, 0.6, 25);
        });
        const finishMethod = () => {
            if (this.yourProbe) {
                this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: action});
            }
        };
        this.finishProbe(finishMethod);
    }

    scanOutside(nodeId) {
        this.schedule.run(50, () => {
            this.animateZoomAndOpacity(SIZE_LARGE, 0.6, 25);
        });
        this.schedule.run(25, () => {
            this.animateZoomAndOpacity(SIZE_MEDIUM_LARGE, 0.3, 25);
        });
        const finishMethod = () => {
            if (this.yourProbe) {
                this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: SCAN_CONNECTIONS});
            }
        };
        this.finishProbe(finishMethod)
    }

    finishProbe(finishMethod) {
        this.schedule.run(10, () => {
            animate(this.canvas, this.probeIcon, 'opacity', "0", 10);
            this.lineIcons.forEach(lineIcon => {
                animate(this.canvas, lineIcon, 'opacity', "0", 10);
            });
            finishMethod();
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.probeIcon);
            this.lineIcons.forEach(lineIcon => {
                this.canvas.remove(lineIcon);
            });
            this.performAutoScan();
        });

    }

    performAutoScan() {
        if (this.autoScan && this.yourProbe) {
            this.dispatch({type: AUTO_SCAN});
        }
    }


    probeError(scanType, nodeId) {
        this.schedule.run(30, () => {
            animate(this.canvas, this.probeIcon, "height", 0, 30)
        });
        this.schedule.run(0, () => {
            this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + scanType + "[/] unknown. nodeId: [info]" + nodeId})
        });
    }

    remove() {
        this.canvas.remove(this.probeIcon);
        this.lineIcons.forEach(lineIcon => {
            this.canvas.remove(lineIcon);
        });
    }

    animateZoomAndOpacity(size, opacity, time) {
        animate(this.canvas, this.probeIcon, 'width', size, time);
        animate(this.canvas, this.probeIcon, 'height', size, time);
        animate(this.canvas, this.probeIcon, 'opacity', opacity, time);
    }

}