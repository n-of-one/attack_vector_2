import {fabric} from "fabric";
import {calcLine, calcLineStart} from "../CanvasUtils";
import {SCAN_CONNECTIONS, SCAN_NODE_DEEP, SCAN_NODE_INITIAL} from "../../../hacker/run/model/NodeScanTypes";
import {Schedule} from "../../Schedule";
import {ConnectionVisual} from "../visuals/ConnectionVisual";
import {COLOR_PROBE_LINE} from "./util/DisplayConstants";
import {TERMINAL_RECEIVE} from "../../terminal/TerminalReducer";
import {Canvas} from "fabric/fabric-impl";
import {Dispatch} from "redux";
import {NodeScanType} from "../../../hacker/run/component/RunCanvas";
import {DisplayCollection} from "./util/DisplayCollection";
import {NodeDisplay} from "./NodeDisplay";
import {Display} from "./Display";
import {webSocketConnection} from "../../WebSocketConnection";
import {Ticks} from "../../model/Ticks";
import {ProbeVisual} from "../visuals/ProbeVisual";



export class ProbeDisplay implements Display {

    canvas: Canvas
    dispatch: Dispatch
    id = null
    schedule
    autoScan
    yourProbe
    aborted = false
    probeVisual: ProbeVisual | null = null

    connectionVisuals: ConnectionVisual[] = []
    padding: number

    x = 0
    y = 0
    size = 0

    constructor(canvas: Canvas, dispatch: Dispatch, path: string[], scanType: NodeScanType, autoScan: boolean,
                startDisplay: Display, yourProbe: boolean, nodeDisplays: DisplayCollection<NodeDisplay>,
                ticks: Ticks) {
        this.canvas = canvas;
        this.schedule = new Schedule(dispatch);
        this.autoScan = autoScan;
        this.yourProbe = yourProbe;
        this.dispatch = dispatch;
        this.padding = yourProbe ? 3: 5;

        let currentDisplay: Display = startDisplay;
        path.forEach((nodeId) => {
            const nextDisplay = nodeDisplays.get(nodeId);
            this.scheduleMoveStep(nextDisplay, currentDisplay);
            currentDisplay = nextDisplay;
        });
        const lastNodeId: string = path.pop()!
        this.schedule.run(0, () => {
            this.processProbeArrive(scanType, lastNodeId, currentDisplay, ticks);
        });

    }

    getAllIcons(): fabric.Object[] {
        const objects: fabric.Object[] = this.probeVisual ? [this.probeVisual.image] : []
        this.connectionVisuals.forEach(element => objects.push(element.line))
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
        const lineElement = new ConnectionVisual(lineStartData, COLOR_PROBE_LINE, this.canvas);

        this.connectionVisuals.push(lineElement);
        const lineData = calcLine(currentDisplay, nextDisplay, this.padding);
        lineElement.extendTo(lineData, duration);
    }

    processProbeArrive(scanType: NodeScanType, nodeId: string, currentDisplay: Display, ticks: Ticks) {
        if (this.aborted) return

        this.probeVisual = new ProbeVisual(this.canvas, currentDisplay as NodeDisplay, this.schedule)

        switch (scanType) {
            case SCAN_NODE_INITIAL:
                return this.scanInside(nodeId, SCAN_NODE_INITIAL, ticks);
            case SCAN_CONNECTIONS:
                return this.scanOutside(nodeId, ticks);
            case SCAN_NODE_DEEP:
                return this.scanInside(nodeId, SCAN_NODE_DEEP, ticks);
            default:
                return this.probeError(scanType, nodeId);
        }
    }

    scanInside(nodeId: string, scanType: NodeScanType, ticks: Ticks) {
        this.probeVisual!.zoomInAndOutAndRemove(ticks)
        const finishMethod = () => {
            if (this.yourProbe) {
                this.probeArriveSaga(nodeId, scanType, this.autoScan)
            }
        };
        this.finishProbe(finishMethod);
    }

    scanOutside(nodeId: string, ticks: Ticks) {
        this.probeVisual!.zoomOutAndInAndRemove(ticks)
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
            this.connectionVisuals.forEach(lineElement => {
                lineElement.disappear(10);
            });
            finishMethod();
        });
        this.schedule.run(0, () => {
            this.probeVisual!.remove()
            this.connectionVisuals.forEach(lineElement => {
                lineElement.remove();
            });
            console.timeEnd("scan");
        });
    }


    probeError(scanType: NodeScanType, nodeId: string) {
        this.probeVisual!.removeError()
        this.schedule.run(0, () => {
            this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + scanType + "[/] unknown. nodeId: [info]" + nodeId})
        });
    }

    remove() {
        this.probeVisual?.remove()
        this.connectionVisuals.forEach(lineElement => {
            lineElement.remove();
        });
    }

    terminate() {
        this.schedule.terminate();
    }
}
