import {fabric} from "fabric";
import {toType} from "../../../common/NodeTypesNames";
import Thread from "../../../common/Thread";
import {TERMINAL_RECEIVE} from "../../../common/terminal/TerminalActions";

/**
 * This class renders the scan map on the JFabric Canvas
 */
class ScanCanvas {


    scan = null;
    nodes = null;
    nodeScanById = null;

    nodesById = {};
    connections = [];
    hackerImage = null;
    dispatch = null;
    thread = new Thread();

    init(dispatch) {
        this.dispatch = dispatch;

        this.canvas = new fabric.StaticCanvas('scanCanvas', {
            width: 607,
            height: 815,
            backgroundColor: "#333333",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.selection = false;
    }

    loadScan(data) {
        const {scan, site} = data;
        const {nodes, connections} = site;
        this.nodeScanById = scan.nodeScanById;
        this.nodes = nodes;
        this.scan = scan;


        this.addHackerImage(nodes[0]);

        nodes.forEach(node => {
            const status = scan.nodeScanById[node.id].status;
            this.addNodeWithAnimation(node, status);
        });

        connections.forEach(connection => {
            this.addConnectionWithAnimation(connection);
        });

    }

    addNodeWithoutRender(node, status) {
        if (!status || status === "UNDISCOVERED") {
            return null;
        }
        const imageStatus = this.determineImageStatus(node, status);
        const type = toType(node.type);
        const imageId = type.name + "_" + imageStatus;

        let image = document.getElementById(imageId);

        let nodeData = {
            id: node.id,
            type: node.type
        };

        let nodeImage = new fabric.Image(image, {
            left: node.x,
            top: node.y,
            height: image.height,
            width: image.width,
            opacity: 0,

            data: nodeData,
        });

        this.canvas.add(nodeImage);
        this.nodesById[node.id] = nodeImage;

        let networkId = node.services[0].data["networkId"];

        let nodeLabel = new fabric.Text(networkId, {
            fill: "#ccc",
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            left: node.x - 20,
            top: node.y + 35,
            textAlign: "left", // "center", "right" or "justify".
            opacity: 0
        });
        this.canvas.add(nodeLabel);
        nodeImage.label = nodeLabel;

        let nodeLabelBackground = new fabric.Rect({
            width: 20,
            height: 20,
            fill: "#333333",
            left: node.x - 20,
            top: node.y + 35,
            opacity: 0
        });
        this.canvas.add(nodeLabelBackground);
        nodeImage.labelBackground = nodeLabelBackground;

        this.canvas.sendToBack(nodeLabelBackground);
        this.canvas.bringToFront(nodeLabel);


        return nodeImage;
    }

    determineImageStatus(node, status) {
        if (status === "DISCOVERED" || status === "TYPE") {
            return status;
        }
        if (status === "CONNECTIONS") {
            return "TYPE";
        }
        if (status === "SERVICES") {
            // TODO implement determining status based on status of node services.
            return "FREE";
        }
    }

    addHackerImage(startNode) {
        let image = document.getElementById("SCORPION");

        this.hackerImage = new fabric.Image(image, {
            left: 607 / 2,
            top: 810,
            height: 40,
            width: 40,
            opacity: 1,

        });

        this.canvas.add(this.hackerImage);

        let line = new fabric.Line(
            [this.hackerImage.left, this.hackerImage.top, startNode.x, startNode.y], {
                stroke: "#bb8",
                strokeWidth: 2,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });
        this.canvas.add(line);
        this.canvas.sendToBack(line);
        this.thread.run(3, () => this.animate(line, "opacity", 0.5, 100));
    }

    render() {
        this.canvas.renderAll();
    }

    addConnectionWithoutRender(connectionData) {
        let fromNode = this.nodesById[connectionData.fromId];
        let toNode = this.nodesById[connectionData.toId];

        if (!fromNode || !toNode) {
            return null;
        }

        let line = new fabric.Line(
            [fromNode.left, fromNode.top, toNode.left, toNode.top], {
                stroke: "#cccccc",
                strokeWidth: 2,
                strokeDashArray: [3, 3],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });

        line.data = connectionData;

        this.canvas.add(line);
        this.connections.push(line);
        this.canvas.sendToBack(line);

        return line;
    }

    addNodeWithAnimation(node, status) {
        const nodeImage = this.addNodeWithoutRender(node, status);
        if (nodeImage) {
            this.thread.run(0, () => {
                this.animate(nodeImage.label, "opacity", 0.5, 40)
            });
            this.thread.run(0, () => {
                this.animate(nodeImage.labelBackground, "opacity", 0.8, 40)
            });
            this.thread.run(3, () => this.animate(nodeImage, "opacity", 0.5, 40));
        }
    }

    addConnectionWithAnimation(connectionData) {
        const lineImage = this.addConnectionWithoutRender(connectionData);
        if (lineImage) {
            this.thread.run(3, () => this.animate(lineImage, "opacity", 0.5, 40));
        }
    }

    animate(toAnimate, attribute, value, duration) {
        toAnimate.animate(attribute, value, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: duration * 50,
            easing: fabric.util.ease.easeInOutSine
        });
    }

    // -- //


    /**
     * "data":{"path":["node-42f2-4f99"],"type":"terminal","value":"burp"}
     */
    launchProbe({path, type, value}) {
        const probeImageNumber = Math.floor(Math.random() * 10) + 1;
        const probeImageElement = document.getElementById("PROBE_" + probeImageNumber);

        let probeImage = new fabric.Image(probeImageElement, {
            left: this.hackerImage.left,
            top: this.hackerImage.top,
            height: 40,
            width: 40,
            opacity: 0,
        });

        this.canvas.add(probeImage);
        this.canvas.bringToFront(probeImage);

        this.animate(probeImage, "opacity", 0.4, 40);

        path.forEach((nodeId) => {
            const nextNodeImage = this.nodesById[nodeId];
            this.thread.run(50, () => this.moveStep(probeImage, nextNodeImage, 50, 5, 5));
        });
        this.thread.run(0, () => {
            this.processProbeArrive(probeImage, type, value);
        });
    }


    moveStep(avatar, node, time, leftDelta, topDelta) {
        this.animate(avatar, 'left', node.left + leftDelta, time );
        this.animate(avatar, 'top', node.top + topDelta, time );
    }

    processProbeArrive(probeImage, type, value) {
        switch(type) {
            case "SCAN_CONNECTIONS": return this.scanConnections(probeImage, value);
            default: return this.probeError(probeImage, type, value);
        }
    }

    scanConnections(probeImage, value) {
        this.thread.run(50, () => {
            this.animate(probeImage, 'width', "100", 50);
            this.animate(probeImage, 'height', "100", 50);
            this.animate(probeImage, 'opacity', "0.2", 50);
        });
        this.thread.run(20, () => {
            this.animate(probeImage, 'width', "40", 50);
            this.animate(probeImage, 'height', "40", 50);
            this.animate(probeImage, 'opacity', "0.4", 50);
        });
    }

    probeError(probeImage, type, value) {
        this.thread.run(30, () => { this.animate(probeImage, "height", 0, 30) });
        this.thread.run(0, () => { this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + type + "[/] unknown. Value: [info]" + value}) });
    }

}

const scanCanvas = new ScanCanvas();
export default scanCanvas