import {fabric} from "fabric";
import {toType} from "../../../common/NodeTypesNames";
import Thread from "../../../common/Thread";
import {TERMINAL_RECEIVE} from "../../../common/terminal/TerminalActions";
import {PROBE_SCAN_NODE} from "../../ScanActions";

/**
 * This class renders the scan map on the JFabric Canvas
 */
class ScanCanvas {


    nodes = null;
    nodeScanById = null;

    iconsById = {};
    connections = [];
    hackerIcon = null;
    dispatch = null;
    iconThread = new Thread();
    probeThread = new Thread();

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


        this.addHackerIcon(nodes[0]);

        nodes.forEach(node => {
            this.addNodeIconWithAnimation(node);
        });

        connections.forEach(connection => {
            this.addConnectionWithAnimation(connection);
        });

    }

    addNode(node) {
        const status = this.nodeScanById[node.id];
        if (!status || status.status === "UNDISCOVERED") {
            return null;
        }
        let nodeData = {
            id: node.id,
            type: node.type
        };

        let image = this.getNodeIconImage(node.id, nodeData);

        let nodeIcon = new fabric.Image(image, {
            left: node.x,
            top: node.y,
            height: image.height,
            width: image.width,
            opacity: 0,

            nodeData: nodeData,
        });

        this.canvas.add(nodeIcon);
        this.iconsById[node.id] = nodeIcon;

        let iconLabel = new fabric.Text(node.networkId, {
            fill: "#ccc",
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            left: node.x - 20,
            top: node.y + 35,
            textAlign: "left", // "center", "right" or "justify".
            opacity: 0
        });
        this.canvas.add(iconLabel);
        nodeIcon.label = iconLabel;

        let labelBackground = new fabric.Rect({
            width: 20,
            height: 20,
            fill: "#333333",
            left: node.x - 20,
            top: node.y + 35,
            opacity: 0
        });
        this.canvas.add(labelBackground);
        nodeIcon.labelBackground = labelBackground;

        this.canvas.sendToBack(labelBackground);
        this.canvas.bringToFront(iconLabel);


        return nodeIcon;
    }

    getNodeIconImage(nodeId, nodeData) {
        const status = this.nodeScanById[nodeId].status;
        const imageStatus = this.determineStatusForIcon(status);
        const type = toType(nodeData.type);
        const imageId = type.name + "_" + imageStatus;
        let image = document.getElementById(imageId);
        return image;
    }

    determineStatusForIcon(status) {
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

    addHackerIcon(startNode) {
        let image = document.getElementById("SCORPION");

        this.hackerIcon = new fabric.Image(image, {
            left: 607 / 2,
            top: 810,
            height: 40,
            width: 40,
            opacity: 1,

        });

        this.canvas.add(this.hackerIcon);

        let line = new fabric.Line(
            [this.hackerIcon.left, this.hackerIcon.top, startNode.x, startNode.y], {
                stroke: "#bb8",
                strokeWidth: 2,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });
        this.canvas.add(line);
        this.canvas.sendToBack(line);
        this.iconThread.run(3, () => this.animate(line, "opacity", 0.5, 100));
    }

    render() {
        this.canvas.renderAll();
    }

    addConnection(connectionData) {
        let fromIcon = this.iconsById[connectionData.fromId];
        let toIcon = this.iconsById[connectionData.toId];

        if (!fromIcon || !toIcon) {
            return null;
        }

        let line = new fabric.Line(
            [fromIcon.left, fromIcon.top, toIcon.left, toIcon.top], {
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

    addNodeIconWithAnimation(node) {
        const nodeIcon = this.addNode(node);
        if (nodeIcon) {
            this.iconThread.run(0, () => { this.animate(nodeIcon.label, "opacity", 0.4, 40) });
            this.iconThread.run(0, () => { this.animate(nodeIcon.labelBackground, "opacity", 0.8, 40) });
            this.iconThread.run(3, () => { this.animate(nodeIcon, "opacity", 0.4, 40)} );
        }
    }

    addConnectionWithAnimation(connectionData) {
        const lineImage = this.addConnection(connectionData);
        if (lineImage) {
            this.iconThread.run(3, () => this.animate(lineImage, "opacity", 0.5, 40));
        }
    }

    animate(toAnimate, attribute, value, duration, easing) {
        const easingFunction = (easing) ? easing: fabric.util.ease.easeInOutSine;
        toAnimate.animate(attribute, value, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: duration * 50,
            easing: easingFunction
        });
    }

    // -- //


    /**
     * "data":{"path":["node-42f2-4f99"],"type":"terminal","value":"burp"}
     */
    launchProbe({path, type, value}) {
        const probeImageNumber = Math.floor(Math.random() * 10) + 1;
        const probeImageElement = document.getElementById("PROBE_" + probeImageNumber);

        let probeIcon = new fabric.Image(probeImageElement, {
            left: this.hackerIcon.left,
            top: this.hackerIcon.top,
            height: 40,
            width: 40,
            opacity: 0,
        });

        this.canvas.add(probeIcon);
        this.canvas.bringToFront(probeIcon);

        this.animate(probeIcon, "opacity", 0.4, 40);

        path.forEach((nodeId) => {
            const nextIcon = this.iconsById[nodeId];
            this.probeThread.run(50, () => this.moveStep(probeIcon, nextIcon, 50, 5, 5));
        });
        const lastNodeId = path.pop();
        this.probeThread.run(0, () => {
            this.processProbeArrive(probeIcon, type, lastNodeId);
        });
    }


    moveStep(avatar, icon, time, leftDelta, topDelta) {
        this.animate(avatar, 'left', icon.left + leftDelta, time );
        this.animate(avatar, 'top', icon.top + topDelta, time );
    }

    processProbeArrive(probeImage, type, nodeId) {
        switch(type) {
            case "SCAN_CONNECTIONS": return this.scanConnections(probeImage, nodeId);
            case "SCAN_NODE_INITIAL": return this.scanInitial(probeImage, nodeId);
            default: return this.probeError(probeImage, type, nodeId);
        }
    }

    scanInitial(probeImage, nodeId) {
        this.probeThread.run(50, () => {
            this.animate(probeImage, 'width', "20", 50);
            this.animate(probeImage, 'height', "20", 50);
            this.animate(probeImage, 'opacity', "0.8", 50);
        });
        this.probeThread.run(25, () => {
            this.animate(probeImage, 'width', "40", 50);
            this.animate(probeImage, 'height', "40", 50);
            this.animate(probeImage, 'opacity', "0.6", 25);
        });
        this.probeThread.run(10, () => {
            this.dispatch({type:PROBE_SCAN_NODE, nodeId: nodeId});
            this.animate(probeImage, 'opacity', "0", 10);
        });
        this.probeThread.run(10, () => {
            this.canvas.remove(probeImage);
            // TODO: start next scan
        });


    }

    scanConnections(probeImage) {
        this.probeThread.run(50, () => {
            this.animate(probeImage, 'width', "100", 50);
            this.animate(probeImage, 'height', "100", 50);
            this.animate(probeImage, 'opacity', "0.2", 50);
        });
        this.probeThread.run(20, () => {
            this.animate(probeImage, 'width', "40", 50);
            this.animate(probeImage, 'height', "40", 50);
            this.animate(probeImage, 'opacity', "0.4", 50);
        });
    }


    probeError(probeImage, type, nodeId) {
        this.probeThread.run(30, () => { this.animate(probeImage, "height", 0, 30) });
        this.probeThread.run(0, () => { this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + type + "[/] unknown. nodeId: [info]" + nodeId}) });
    }

    probeSuccess(nodeId, newStatus) {
        this.nodeScanById[nodeId].status = newStatus;
        this.updateNodeIcon(nodeId, true);
    }

    updateNodeIcon = (nodeId, fadeOut) => {
        const icon = this.iconsById[nodeId];
        if (fadeOut) {
            this.iconThread.run(8, () => {
                this.animate(icon, 'opacity', 0.05, 8, fabric.util.ease.easeOutSine);
                this.animate(icon, 'left', "-=10", 8, fabric.util.ease.easeOutSine);
            });
        }

        const newIconImage = this.getNodeIconImage(nodeId, icon.nodeData);

        this.iconThread.run(0, () => {
            icon.setElement(newIconImage);
            this.canvas.renderAll();
            const newOpacity = 0.4;
            // const newOpacity = nodeOpacityByStatus[node.status];
            this.animate(icon, 'opacity', newOpacity, 8);
            if (fadeOut) {
                this.animate(icon, 'left', "+=10", 8, fabric.util.ease.easeInSine);
            }
        });
    };

}

const scanCanvas = new ScanCanvas();
export default scanCanvas