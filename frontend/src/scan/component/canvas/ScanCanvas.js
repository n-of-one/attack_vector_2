import {fabric} from "fabric";
import {toType} from "../../../common/NodeTypesNames";
import Thread from "../../../common/Thread";
import {TERMINAL_RECEIVE} from "../../../common/terminal/TerminalActions";
import {AUTO_SCAN, PROBE_SCAN_NODE} from "../../ScanActions";
import {SCAN_CONNECTIONS, SCAN_NODE_DEEP, SCAN_NODE_INITIAL} from "../../NodeScanTypes";
import {CONNECTIONS, DISCOVERED, FREE, SERVICES, TYPE, UNDISCOVERED} from "../../../common/enums/NodeStatus";
import Threads from "../../../common/Threads";

/**
 * This class renders the scan map on the JFabric Canvas
 */
class ScanCanvas {

    nodes = null;
    nodeScanById = null;

    iconsById = {};
    connectionsById = {};
    hackerIcon = null;
    dispatch = null;
    iconThread = new Thread();
    probeThreads = new Threads();

    userId = null;

    init(userId, dispatch) {
        this.userId = userId;
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
        this.nodesById = {};
        nodes.forEach((node) => {
            this.nodesById[node.id] = node;
        });
        this.connectionsById = {};
        connections.forEach((connection) => {
            this.connectionsById[connection.id] = connection;
        });

        this.addHackerIcon(nodes[0]);

        nodes.forEach(node => {
            const status = this.nodeScanById[node.id];
            if (status && status.status !== UNDISCOVERED) {
                this.addNodeIconWithAnimation(node);
            }

        });

        connections.forEach(connection => {
            const fromIcon = this.iconsById[connection.fromId];
            const toIcon = this.iconsById[connection.toId];

            if (fromIcon && toIcon) {
                this.addConnectionIconWithAnimation(fromIcon, toIcon);
            }
        });

    }

    addNodeIcon(node) {
        const nodeData = {
            id: node.id,
            type: node.type
        };

        const image = this.getNodeIconImage(node.id, nodeData);

        const nodeIcon = new fabric.Image(image, {
            left: node.x,
            top: node.y,
            height: image.height,
            width: image.width,
            opacity: 0,

            nodeData: nodeData,
        });

        this.canvas.add(nodeIcon);
        this.iconsById[node.id] = nodeIcon;

        const iconLabel = new fabric.Text(node.networkId, {
            // fill: "#bbbbbb", // just simple grey
            fill: "#5cb85c",    // color-ok
            // fill: "#8cad8c", // color-ok muted (more grey)
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            // fontWeight: 10,
            left: node.x - 20,
            top: node.y + 35,
            textAlign: "left", // "center", "right" or "justify".
            opacity: 0
        });
        this.canvas.add(iconLabel);
        nodeIcon.label = iconLabel;

        const labelBackground = new fabric.Rect({
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
        const image = document.getElementById(imageId);
        return image;
    }

    determineStatusForIcon(status) {
        if (status === DISCOVERED || status === TYPE) {
            return status;
        }
        if (status === CONNECTIONS) {
            return TYPE;
        }
        if (status === SERVICES) {
            // TODO implement determining status based on status of node services.
            return FREE;
        }
    }

    addHackerIcon(startNode) {
        const image = document.getElementById("SCORPION");

        this.hackerIcon = new fabric.Image(image, {
            left: 607 / 2,
            top: 810,
            height: 40,
            width: 40,
            opacity: 1,

        });

        this.canvas.add(this.hackerIcon);

        const line = new fabric.Line(
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

    addConnectionIcon(fromIcon, toIcon) {
        const line = new fabric.Line(
            [fromIcon.left, fromIcon.top, toIcon.left, toIcon.top], {
                stroke: "#cccccc",
                strokeWidth: 2,
                strokeDashArray: [3, 3],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });

        this.canvas.add(line);
        this.canvas.sendToBack(line);

        return line;
    }

    addNodeIconWithAnimation(node) {
        const nodeIcon = this.addNodeIcon(node);
        this.iconThread.run(0, () => {
            this.animate(nodeIcon.label, "opacity", 1, 40)
        });
        this.iconThread.run(0, () => {
            this.animate(nodeIcon.labelBackground, "opacity", 0.8, 40)
        });
        this.iconThread.run(3, () => {
            this.animate(nodeIcon, "opacity", 0.4, 40)
        });
    }

    addConnectionIconWithAnimation(fromIcon, toIcon) {
        const lineIcon = this.addConnectionIcon(fromIcon, toIcon);
        this.iconThread.run(3, () => this.animate(lineIcon, "opacity", 0.5, 40));
    }

    animate(toAnimate, attribute, value, duration, easing) {
        const easingFunction = (easing) ? easing : fabric.util.ease.easeInOutSine;
        toAnimate.animate(attribute, value, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: duration * 50,
            easing: easingFunction
        });
    }

    launchProbe({userId, path, scanType, autoScan}) {
        const probeUserId = userId;
        const probeImageNumber = Math.floor(Math.random() * 10) + 1;
        const probeImageElement = document.getElementById("PROBE_" + probeImageNumber);

        const probeIcon = new fabric.Image(probeImageElement, {
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
            this.probeThreads.run(probeUserId, 20, () => this.moveStep(probeIcon, nextIcon, 20, 5, 5));
        });
        const lastNodeId = path.pop();
        this.probeThreads.run(userId, 0, () => {
            this.processProbeArrive(probeUserId, probeIcon, scanType, lastNodeId, autoScan);
        });
    }


    moveStep(avatar, icon, time, leftDelta, topDelta) {
        this.animate(avatar, 'left', icon.left + leftDelta, time);
        this.animate(avatar, 'top', icon.top + topDelta, time);
    }

    processProbeArrive(probeUserId, probeImage, scanType, nodeId, autoScan) {
        switch (scanType) {
            case SCAN_NODE_INITIAL:
                return this.scanInside(probeUserId, probeImage, nodeId, SCAN_NODE_INITIAL, autoScan);
            case SCAN_CONNECTIONS:
                return this.scanOutside(probeUserId, probeImage, nodeId, autoScan);
            case SCAN_NODE_DEEP:
                return this.scanInside(probeUserId, probeImage, nodeId, SCAN_NODE_DEEP, autoScan);
            default:
                return this.probeError(probeUserId, probeImage, scanType, nodeId);
        }
    }

    scanInside(probeUserId, probeImage, nodeId, action, autoScan) {
        this.probeThreads.run(probeUserId, 50, () => {
            this.animate(probeImage, 'width', "20", 50);
            this.animate(probeImage, 'height', "20", 50);
            this.animate(probeImage, 'opacity', "0.8", 50);
        });
        this.probeThreads.run(probeUserId, 25, () => {
            this.animate(probeImage, 'width', "40", 50);
            this.animate(probeImage, 'height', "40", 50);
            this.animate(probeImage, 'opacity', "0.6", 25);
        });
        this.probeThreads.run(probeUserId, 10, () => {
            this.animate(probeImage, 'opacity', "0", 10);
            if (this.userId === probeUserId) {
                this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: action});
            }
        });

        this.probeThreads.run(probeUserId, 10, () => {
            this.canvas.remove(probeImage);
            this.autoScan(probeUserId, autoScan);
        });
    }

    scanOutside(probeUserId, probeImage, nodeId, autoScan) {
        this.probeThreads.run(probeUserId, 50, () => {
            this.animate(probeImage, 'width', "100", 50);
            this.animate(probeImage, 'height', "100", 50);
            this.animate(probeImage, 'opacity', "0.2", 50);
        });
        this.probeThreads.run(probeUserId, 25, () => {
            this.animate(probeImage, 'width', "40", 50);
            this.animate(probeImage, 'height', "40", 50);
            this.animate(probeImage, 'opacity', "0.3", 25);
        });
        this.probeThreads.run(probeUserId, 10, () => {
            this.animate(probeImage, 'opacity', "0", 10);
            if (this.userId === probeUserId) {
                this.dispatch({type: PROBE_SCAN_NODE, nodeId: nodeId, action: SCAN_CONNECTIONS});
            }
        });
        this.probeThreads.run(probeUserId, 10, () => {
            this.canvas.remove(probeImage);
            this.autoScan(probeUserId, autoScan);
        });
    }

    autoScan(probeUserId, autoScan) {
        if (autoScan && probeUserId === this.userId) {
            this.dispatch({type: AUTO_SCAN});
        }

    }

    probeError(probeUserId, probeImage, scanType, nodeId) {
        this.probeThreads.run(probeUserId, 30, () => {
            this.animate(probeImage, "height", 0, 30)
        });
        this.probeThreads.run(probeUserId, 0, () => {
            this.dispatch({type: TERMINAL_RECEIVE, data: "[warn b]Probe error: [info]" + scanType + "[/] unknown. nodeId: [info]" + nodeId})
        });
    }

    updateNodeStatus(nodeId, newStatus) {
        this.nodeScanById[nodeId].status = newStatus;
        if (newStatus !== CONNECTIONS) {
            this.updateNodeIcon(nodeId, true);
        }
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

    discoverNodes(nodeIds, connectionIds) {
        nodeIds.forEach((id) => {
            const node = this.nodesById[id];
            this.nodeScanById[id].status = DISCOVERED;
            this.addNodeIconWithAnimation(node);
        });

        connectionIds.forEach((id) => {
            const connection = this.connectionsById[id];
            const fromIcon = this.iconsById[connection.fromId];
            const toIcon = this.iconsById[connection.toId];
            this.addConnectionIconWithAnimation(fromIcon, toIcon)
        });
    }
}

const scanCanvas = new ScanCanvas();
export default scanCanvas