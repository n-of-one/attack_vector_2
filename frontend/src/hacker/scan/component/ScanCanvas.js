import {fabric} from "fabric";
import Thread from "../../../common/Thread";
import {DISCOVERED, UNDISCOVERED} from "../../../common/enums/NodeStatus";
import Threads from "../../../common/Threads";
import NodeDisplay from "../../../common/canvas/NodeDisplay";
import ConnectionIcon from "../../../common/canvas/ConnectionDisplay";
import HackerIcon from "../../../common/canvas/HackerDisplay";
import ProbeDisplay from "../../../common/canvas/ProbeDisplay";

/**
 * This class renders the scan map on the JFabric Canvas
 */
class ScanCanvas {

    nodeDataById = null;
    connectionDataById = {};

    displayById = {};

    hackerDisplay = null;

    dispatch = null;
    iconThread = new Thread();
    probeThreads = new Threads();

    userId = null;

    canvas = null;

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

    reset() {
        if (this.canvas === null) {
            return;
        }

        Object.keys(this.displayById).forEach((id) => {
            const icon = this.displayById[id];

            this.canvas.remove(icon);
        });

        this.nodeDataById = null;
        this.connections = [];

        this.displayById = {};
        this.connectionDataById = {};
        this.hackerDisplay = null;
        this.iconThread = new Thread();
        this.probeThreads = new Threads();

        this.render();
    }

    render() {
        this.canvas.renderAll();
    }

    loadScan(data) {
        const {scan, site} = data;
        const {nodes, connections} = site;
        this.nodeDataById = {};
        nodes.forEach((nodeData) => {
            this.nodeDataById[nodeData.id] = nodeData;
            const nodeScan = scan.nodeScanById[nodeData.id];
            nodeData.status = nodeScan.status;
            nodeData.distance = nodeScan.distance;

        });
        this.connectionDataById = {};
        connections.forEach((connectionData) => {
            this.connectionDataById[connectionData.id] = connectionData;
        });

        this.addHackerDisplay(nodes[0]);

        nodes.forEach(node => {
            if (node.status !== UNDISCOVERED) {
                this.addNodeDisplay(node);
            }
        });

        connections.forEach(connection => {
            const fromIcon = this.displayById[connection.fromId];
            const toIcon = this.displayById[connection.toId];

            if (fromIcon && toIcon) {
                this.addConnectionDisplay(connection.id, fromIcon, toIcon);
            }
        });

    }


    addHackerDisplay(startNode) {
        this.hackerDisplay = new HackerIcon(this.canvas, this.iconThread, startNode, this.iconThread);
        this.displayById["user-fixme-123"] = this.hackerDisplay;
    }

    addNodeDisplay(node) {
        const nodeDisplay = new NodeDisplay(this.canvas, this.iconThread, node);
        this.displayById[node.id] = nodeDisplay;
        nodeDisplay.appear();
    }

    addConnectionDisplay(id, fromIcon, toIcon) {
        const connectionDisplay = new ConnectionIcon(this.canvas, this.iconThread, id, fromIcon, toIcon);
        this.displayById[id] = connectionDisplay;
        connectionDisplay.appear();
    }

    launchProbe(probeData) {
        const probeThread = this.probeThreads.getOrCreateThread(probeData.probeUserId);
        const probeDisplay = new ProbeDisplay(this.canvas, probeThread, this.dispatch, probeData, this.userId, this.hackerDisplay, this.displayById)
        // FIXME: add this to allow removal of probe when switching away from scan
        // this.displayById[probeData.id] = probeDisplay

    }

    updateNodeStatus(nodeId, newStatus) {
        this.displayById[nodeId].updateStatus(newStatus);
    }

    discoverNodes(nodeIds, connectionIds) {
        nodeIds.forEach((id) => {
            const nodeData = this.nodeDataById[id];
            nodeData.status = DISCOVERED;
            this.addNodeDisplay(nodeData);
        });

        connectionIds.forEach((id) => {
            const connection = this.connectionDataById[id];
            const fromIcon = this.displayById[connection.fromId];
            const toIcon = this.displayById[connection.toId];
            this.addConnectionDisplay(connection.id, fromIcon, toIcon)
        });
    }

}

const scanCanvas = new ScanCanvas();
export default scanCanvas