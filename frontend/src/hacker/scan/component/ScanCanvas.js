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
    hackers = [];

    displayById = {};

    // hackerDisplay = null;
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
        this.hackers = [];

        this.displayById = {};
        this.connectionDataById = {};
        this.hackerDisplay = null;

        this.iconThread.deactivate();
        this.probeThreads.deactivate();
        this.iconThread = new Thread();
        this.probeThreads = new Threads();


        this.render();
    }

    render() {
        this.canvas.renderAll();
    }

    loadScan(data) {
        const {scan, site, hackers} = data;
        const {nodes, connections} = site;
        this.nodeDataById = {};
        this.sortAndaddHackers(hackers);
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

        nodes.forEach(node => {

            if (node.status !== UNDISCOVERED) {
                this.addNodeDisplay(node);
            }
        });
        connections.forEach(connection => {

            const fromDisplay = this.displayById[connection.fromId];
            const toDisplay = this.displayById[connection.toId];
            if (fromDisplay && toDisplay) {

                this.addConnectionDisplay(connection.id, fromDisplay, toDisplay);
            }
        });

        const startNodeDisplay = this.displayById[nodes[0].id];
        this.addHackersDisplays(startNodeDisplay);
    }

    sortAndaddHackers(hackers) {
        const you = hackers.find(hacker => hacker.userId === this.userId );
        const others = hackers.filter(hacker => hacker.userId !== this.userId);
        others.sort((a, b) => (a.userName > b.userName) ? 1 : ((b.userName > a.userName) ? -1 : 0));
        const midIndex = Math.floor(others.length / 2);
        this.hackers = others;
        this.hackers.splice(midIndex, 0, you);
    }

    addHackersDisplays(startNodeDisplay) {
        const step = Math.floor(607 / (this.hackers.length+1));
        this.hackers.forEach((hacker, index) => {
            this.addHackerDisplay(hacker, startNodeDisplay, step * (index+1))
        });
    }

    addHackerDisplay(hacker, startNodeDisplay, offset) {
        const you = hacker.userId === this.userId;
        const hackerDisplay= new HackerIcon(this.canvas, this.iconThread, startNodeDisplay, hacker, offset, you);
        this.displayById[hacker.userId] = hackerDisplay;
        if (you) {
            // this.hackerDisplay = hackerDisplay;
        }
    }

    addNodeDisplay(node) {
        const nodeDisplay = new NodeDisplay(this.canvas, this.iconThread, node);
        this.displayById[node.id] = nodeDisplay;
        nodeDisplay.appear();
    }

    addConnectionDisplay(id, fromDisplay, toDisplay) {
        const connectionDisplay = new ConnectionIcon(this.canvas, this.iconThread, id, fromDisplay, toDisplay);
        this.displayById[id] = connectionDisplay;
        connectionDisplay.appear();
    }

    launchProbe(probeData) {
        const probeThread = this.probeThreads.getOrCreateThread(probeData.probeUserId);
        const hackerDisplay = this.displayById[probeData.probeUserId];
        const yourProbe = probeData.probeUserId === this.userId;
        const probeDisplay = new ProbeDisplay(this.canvas, probeThread, this.dispatch, probeData, hackerDisplay, yourProbe, this.displayById);
        this.displayById[probeData.id] = probeDisplay;

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