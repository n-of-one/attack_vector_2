import {fabric} from "fabric";
import {animate} from "./CanvasUtils";
import {AUTO_SCAN, PROBE_SCAN_NODE} from "../../hacker/scan/model/ScanActions";
import {SCAN_CONNECTIONS, SCAN_NODE_DEEP, SCAN_NODE_INITIAL} from "../../hacker/scan/model/NodeScanTypes";
import {TERMINAL_RECEIVE} from "../terminal/TerminalActions";

export default class ConnectionDisplay {

    canvas = null;
    dispatch = null;

    id = null;
    thread = null;
    autoScan = null;
    userId = null;
    probeIcon = null;

    constructor(canvas, thread, dispatch, {probeUserId, path, scanType, autoScan}, userId, hackerDisplay, displayById) {
        this.canvas = canvas;
        this.thread = thread;
        this.autoScan = autoScan;
        this.userId = userId;
        this.probeUserId = probeUserId;
        this.dispatch = dispatch;


        const imageNumber = Math.floor(Math.random() * 10) + 1;
        const image = document.getElementById("PROBE_" + imageNumber);

        this.probeIcon = new fabric.Image(image, {
            left: hackerDisplay.x,
            top: hackerDisplay.y,
            height: 40,
            width: 40,
            opacity: 0,
        });

        this.canvas.add(this.probeIcon);
        this.canvas.bringToFront(this.probeIcon);

        animate(this.canvas, this.probeIcon, "opacity", 0.4, 40);

        path.forEach((nodeId) => {
            const nextDisplay = displayById[nodeId];
            thread.run(20, () => this.moveStep(nextDisplay, 20, 5, 5));
        });
        const lastNodeId = path.pop();
        thread.run(0, () => {
            this.processProbeArrive(scanType, lastNodeId);
        });
    }


    moveStep(nodeDisplay, time, leftDelta, topDelta) {
        animate(this.canvas, this.probeIcon, 'left', nodeDisplay.x + leftDelta, time);
        animate(this.canvas, this.probeIcon, 'top', nodeDisplay.y + topDelta, time);
    }



    processProbeArrive(scanType, nodeId) {
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
        this.thread.run(50, () => {
            animate(this.canvas, this.probeIcon, 'width', "20", 50);
            animate(this.canvas, this.probeIcon, 'height', "20", 50);
            animate(this.canvas, this.probeIcon, 'opacity', "0.8", 50);
        });
        this.thread.run(25, () => {
            animate(this.canvas, this.probeIcon, 'width', "40", 50);
            animate(this.canvas, this.probeIcon, 'height', "40", 50);
            animate(this.canvas, this.probeIcon, 'opacity', "0.6", 25);
        });
        this.thread.run(10, () => {
            animate(this.canvas, this.probeIcon, 'opacity', "0", 10);
            if (this.userId === this.probeUserId) {
                this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: action});
            }
        });

        this.thread.run(10, () => {
            this.canvas.remove(this.probeIcon);
            this.performAutoScan();
        });
    }

    scanOutside(nodeId) {
        this.thread.run(50, () => {
            animate(this.canvas, this.probeIcon, 'width', "100", 50);
            animate(this.canvas, this.probeIcon, 'height', "100", 50);
            animate(this.canvas, this.probeIcon, 'opacity', "0.2", 50);
        });
        this.thread.run(25, () => {
            animate(this.canvas, this.probeIcon, 'width', "40", 50);
            animate(this.canvas, this.probeIcon, 'height', "40", 50);
            animate(this.canvas, this.probeIcon, 'opacity', "0.3", 25);
        });
        this.thread.run(10, () => {
            animate(this.canvas, this.probeIcon, 'opacity', "0", 10);
            if (this.userId === this.probeUserId) {
                this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: SCAN_CONNECTIONS});
            }
        });
        this.thread.run(10, () => {
            this.canvas.remove(this.probeIcon);
            this.performAutoScan();
        });
    }

    performAutoScan() {
        if (this.autoScan && this.probeUserId === this.userId) {
            this.dispatch({type: AUTO_SCAN});
        }
    }


    probeError(scanType, nodeId) {
        this.thread.run(30, () => {
            animate(this.canvas, this.probeIcon, "height", 0, 30)
        });
        this.thread.run(0, () => {
            this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + scanType + "[/] unknown. nodeId: [info]" + nodeId})
        });
    }

}