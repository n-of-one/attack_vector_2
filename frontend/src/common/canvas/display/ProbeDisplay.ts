import {fabric} from "fabric";
import {animate, calcLine, calcLineStart, getHtmlImage} from "../CanvasUtils";
import {SCAN_CONNECTIONS, SCAN_NODE_DEEP, SCAN_NODE_INITIAL} from "../../../hacker/run/model/NodeScanTypes";
import {Schedule} from "../../Schedule";
import {LineElement} from "./util/LineElement";
import {COLOR_PROBE_LINE} from "./util/DisplayConstants";
import {TERMINAL_RECEIVE} from "../../terminal/TerminalReducer";
import {Canvas} from "fabric/fabric-impl";
import {Dispatch} from "redux";
import {NodeScanType} from "../../../hacker/run/component/RunCanvas";
import {HackerDisplay} from "./HackerDisplay";
import {DisplayCollection} from "./util/DisplayCollection";
import {NodeDisplay} from "./NodeDisplay";
import {Display} from "./Display";
import {webSocketConnection} from "../../WebSocketConnection";

const SIZE_SMALL = (20/40);
const SIZE_SMALL_MEDIUM = (30/40);
const SIZE_MEDIUM = (40/40);
const SIZE_LARGE = (80/40);
const SIZE_MEDIUM_LARGE = (58/40);

const PROBE_IMAGE_SIZE = 40;

export class ProbeDisplay implements Display {

    canvas: Canvas
    dispatch: Dispatch
    id = null
    schedule
    autoScan
    yourProbe
    probeIcon
    aborted = false

    lineElements: LineElement[] = []
    padding: number

    x = 0
    y = 0
    size = 0



    constructor(canvas: Canvas, dispatch: Dispatch, path: string[], scanType: NodeScanType, autoScan: boolean,
                hackerDisplay: HackerDisplay, yourProbe: boolean, nodeDisplays: DisplayCollection<NodeDisplay>) {
        this.canvas = canvas;
        this.schedule = new Schedule(dispatch);
        this.autoScan = autoScan;
        this.yourProbe = yourProbe;
        this.dispatch = dispatch;
        this.padding = yourProbe ? 3: 5;

        const imageNumber = Math.floor(Math.random() * 10) + 1;
        const image = getHtmlImage("PROBE_" + imageNumber)

        this.probeIcon = new fabric.Image(image, {
            left: hackerDisplay.x,
            top: hackerDisplay.y,
            height: PROBE_IMAGE_SIZE,
            width: PROBE_IMAGE_SIZE,
            scaleX: SIZE_MEDIUM,
            scaleY: SIZE_MEDIUM,
            opacity: 0,
            selectable: false,
        });

        this.canvas.add(this.probeIcon);
        this.canvas.bringToFront(this.probeIcon);

        let currentDisplay: Display = hackerDisplay;
        path.forEach((nodeId) => {
            const nextDisplay = nodeDisplays.get(nodeId);
            this.scheduleMoveStep(nextDisplay, currentDisplay);
            currentDisplay = nextDisplay;
        });
        const lastNodeId: string = path.pop()!
        this.schedule.run(0, () => {
            this.processProbeArrive(scanType, lastNodeId, currentDisplay);
        });

    }

    getAllIcons(): fabric.Object[] {
        const objects: fabric.Object[] = [this.probeIcon]
        this.lineElements.forEach( element => objects.push(element.line))
        return objects
    }

    // TODO: scans should be abortable if the hacker leaves the run where this scan is taking place.
    abort() {
        this.aborted = true
    }

    scheduleMoveStep(nextDisplay: Display, currentDisplay: Display) {
        this.schedule.run(22, () => this.moveStep(nextDisplay, currentDisplay, 20));
    }

    moveStep(nextDisplay: Display, currentDisplay: Display, duration: number) {
        if (this.aborted) return

        const lineStartData = calcLineStart(currentDisplay, nextDisplay, 22, this.padding);
        const lineElement = new LineElement(lineStartData, COLOR_PROBE_LINE, this.canvas);

        this.lineElements.push(lineElement);
        const lineData = calcLine(currentDisplay, nextDisplay, this.padding);
        lineElement.extendTo(lineData, duration);
    }

    processProbeArrive(scanType: NodeScanType, nodeId: string, currentDisplay: Display) {
        if (this.aborted) return

        this.probeIcon.left = (currentDisplay.x + 5);
        this.probeIcon.top = (currentDisplay.y + 5);

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

    scanInside(nodeId: string, scanType: NodeScanType) {
        this.schedule.run(50, () => {
            console.time("scan");
            this.animateZoom(SIZE_SMALL, 50);
            this.animateOpacity(0.9, 50);

        });
        this.schedule.run(25, () => {
            this.animateZoom(SIZE_SMALL_MEDIUM, 25);
            this.animateOpacity(0, 35);
        });
        const finishMethod = () => {
            if (this.yourProbe) {
                this.probeArriveSaga(nodeId, scanType, this.autoScan)
            }
        };
        this.finishProbe(finishMethod);
    }

    scanOutside(nodeId: string) {
        this.schedule.run(50, () => {
            console.time("scan");
            this.animateZoom(SIZE_LARGE, 50);
            this.animateOpacity(0.7, 50);
        });
        this.schedule.run(25, () => {
            this.animateZoom(SIZE_MEDIUM_LARGE, 35);
            this.animateOpacity(0, 35);
        });
        const finishMethod = () => {
            if (this.yourProbe) {
                this.probeArriveSaga(nodeId, SCAN_CONNECTIONS, this.autoScan)
            }
        };
        this.finishProbe(finishMethod)
    }

    probeArriveSaga(nodeId: string, scanType: NodeScanType, autoScan: boolean) {
        const payload = {nodeId: nodeId, action: scanType, autoScan: autoScan};
        webSocketConnection.sendObjectWithRunId("/av/scan/probeArrive", payload);
    }

    finishProbe(finishMethod : () => void) {
        this.schedule.run(10, () => {
            this.lineElements.forEach(lineElement => {
                lineElement.disappear(10);
            });
            finishMethod();
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.probeIcon);
            this.lineElements.forEach(lineElement => {
                lineElement.remove();
            });
            console.timeEnd("scan");
        });
    }


    probeError(scanType: NodeScanType, nodeId: string) {
        this.schedule.run(30, () => {
            animate(this.canvas, this.probeIcon, "scaleY", 0, 30)
        });
        this.schedule.run(0, () => {
            this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + scanType + "[/] unknown. nodeId: [info]" + nodeId})
        });
    }

    remove() {
        this.canvas.remove(this.probeIcon);
        this.lineElements.forEach(lineElement => {
            lineElement.remove();
        });
    }

    animateZoom(scale: number, duration: number) {
        animate(this.canvas, this.probeIcon, 'scaleX', scale, duration);
        animate(this.canvas, this.probeIcon, 'scaleY', scale, duration);
    }
    animateOpacity(opacity: number, duration: number) {
        animate(this.canvas, this.probeIcon, 'opacity', opacity, duration);
    }

    terminate() {
        this.schedule.terminate();
    }
}
