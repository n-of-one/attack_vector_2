import {fabric} from "fabric";
import Schedule from "../../../common/Schedule";
import {DISCOVERED, UNDISCOVERED} from "../../../common/enums/NodeStatus";
import NodeDisplay from "../../../common/canvas/NodeDisplay";
import ConnectionIcon from "../../../common/canvas/ConnectionDisplay";
import HackerIcon from "../../../common/canvas/HackerDisplay";
import ProbeDisplay from "../../../common/canvas/ProbeDisplay";
import {CANVAS_HEIGHT, CANVAS_WIDTH} from "../../../common/canvas/CanvasConst";
import {DISPLAY_NODE_INFO, HIDE_NODE_INFO} from "../model/ScanActions";

/**
 * This class renders the scan map on the JFabric Canvas
 */
class RunCanvas {

    nodeDataById = null;
    connectionDataById = {};
    hackers = [];

    displayById = {};

    dispatch = null;
    iconSchedule = new Schedule();

    startNodeDisplay = null;
    userId = null;

    canvas = null;
    selectedObject = null;
    hacking = false;


    init(userId, dispatch) {
        this.userId = userId;
        this.dispatch = dispatch;

        this.canvas = new fabric.Canvas('runCanvas', {
            width: CANVAS_WIDTH,
            height: CANVAS_HEIGHT,
            backgroundColor: "#333333",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.selection = false;
        this.canvas.on('object:selected', (event) => { this.canvasObjectSelected(event); });
        this.canvas.on('selection:cleared', (event) => { this.canvasObjectDeSelected(event); });

    }

    reset() {
        if (this.canvas === null) {
            return;
        }

        Object.keys(this.displayById).forEach((id) => {
            const icon = this.displayById[id];
            this.canvas.remove(icon);
        });

        this.selectedObject = null;

        this.nodeDataById = null;
        this.connections = [];
        this.hackers = [];

        this.displayById = {};
        this.connectionDataById = {};
        this.startNodeDisplay = null;

        this.iconSchedule.deactivate();
        this.iconSchedule = new Schedule();
        this.hacking = false;

        this.render();
    }

    render() {
        this.canvas.renderAll();
    }

    loadScan(data) {
        const {scan, site, hackers} = data;
        const {nodes, connections} = site;
        this.nodeDataById = {};
        this.sortAndAddHackers(hackers);
        nodes.forEach((nodeData) => {
            this.nodeDataById[nodeData.id] = nodeData;
            const nodeScan = scan.nodeScanById[nodeData.id];
            nodeData.status = nodeScan.status;
            nodeData.distance = nodeScan.distance;

        });
        nodes.forEach(node => {
            if (node.status !== UNDISCOVERED) {
                this.addNodeDisplay(node);
            }
        });

        this.connectionDataById = {};
        connections.forEach((connectionData) => {
            this.connectionDataById[connectionData.id] = connectionData;
        });
        connections.forEach(connection => {

            const fromDisplay = this.displayById[connection.fromId];
            const toDisplay = this.displayById[connection.toId];
            if (fromDisplay && toDisplay) {

                this.addConnectionDisplay(connection.id, fromDisplay, toDisplay);
            }
        });

        this.startNodeDisplay = this.displayById[nodes[0].id];
        this.addHackersDisplays();
    }

    sortAndAddHackers(hackers) {
        const you = hackers.find(hacker => hacker.userId === this.userId);
        const others = hackers.filter(hacker => hacker.userId !== this.userId);
        others.sort((a, b) => (a.userName > b.userName) ? 1 : ((b.userName > a.userName) ? -1 : 0));
        const midIndex = Math.floor(others.length / 2);
        this.hackers = others;
        this.hackers.splice(midIndex, 0, you);
    }

    addHackersDisplays() {
        const step = Math.floor(CANVAS_WIDTH / (this.hackers.length + 1));
        this.hackers.forEach((hacker, index) => {
            this.addHackerDisplay(hacker, step * (index + 1))
        });
    }

    addHackerDisplay(hacker, offset) {
        const you = hacker.userId === this.userId;
        const currentNode = (hacker.hacking) ? this.displayById[hacker.nodeId] : null;
        this.displayById[hacker.userId] = new HackerIcon(this.canvas, this.startNodeDisplay, hacker, offset, you, this.dispatch, currentNode);
    }

    removeHackerDisplay(hacker) {
        const hackerDisplay = this.displayById[hacker.userId];
        delete this.displayById[hacker.userId];
        hackerDisplay.disappear();
    }

    addNodeDisplay(node) {
        const nodeDisplay = new NodeDisplay(this.canvas, this.iconSchedule, node, true, this.hacking);
        this.displayById[node.id] = nodeDisplay;
        nodeDisplay.appear();
    }

    addConnectionDisplay(id, fromDisplay, toDisplay) {
        const connectionDisplay = new ConnectionIcon(this.canvas, this.iconSchedule, id, fromDisplay, toDisplay);
        this.displayById[id] = connectionDisplay;
        connectionDisplay.appear();
    }

    launchProbe(probeData) {
        const hackerDisplay = this.displayById[probeData.probeUserId];
        const yourProbe = probeData.probeUserId === this.userId;
        this.displayById[probeData.id] = new ProbeDisplay(this.canvas, this.dispatch, probeData, hackerDisplay, yourProbe, this.displayById);
    }

    updateNodeStatus(nodeId, newStatus) {
        this.displayById[nodeId].updateStatus(newStatus, this.selectedObject);
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

    hackerEnter(newHacker) {
        if (newHacker.userId === this.userId) {
            return;
        }
        if (this.hackers.length % 2 === 0) {
            // Even number of hackers: hacker get's added to the left.
            this.hackers.splice(0, 0, newHacker);
        } else {
            // Odd: add to right.
            this.hackers.push(newHacker);
        }
        this.repositionHackers(newHacker);
    }

    hackerLeave(leavingHacker) {
        if (leavingHacker.userId === this.userId) {
            return
        }
        this.removeHackerDisplay(leavingHacker);
        this.hackers = this.hackers.filter(element => element.userId !== leavingHacker.userId);
        this.repositionHackers(leavingHacker);
    }

    repositionHackers(targetHacker) {
        const step = Math.floor(CANVAS_WIDTH / (this.hackers.length + 1));
        this.hackers.forEach((hacker, index) => {
            const newX = step * (index + 1);
            if (hacker.userId === targetHacker.userId) {
                this.addHackerDisplay(hacker, newX)
            }
            else {
                this.displayById[hacker.userId].repositionHackerIdentification(newX);
            }
        });
    }

    canvasObjectSelected(event) {
        if (event.target && event.target.type === "node") {
            this.selectedObject = event.target;
            this.dispatch({type: DISPLAY_NODE_INFO, nodeId: event.target.data.id});
        }
    }

    canvasObjectDeSelected() {
        this.selectedObject = null;
        this.dispatch({type: HIDE_NODE_INFO});
    }

    unSelect() {
        this.canvas.deactivateAll().renderAll();
        this.canvasObjectDeSelected();
    }

    startAttack(userId, quick) {
        this.hacking = true;
        if (this.userId === userId) {
            if (!quick) {
                this.iconSchedule.wait(30);
            }
            this.forAllNodeDisplays((nodeDisplay) => {nodeDisplay.transitionToHack(quick)});
            this.forAllNodeDisplays((nodeDisplay) => {nodeDisplay.cleanUpAfterCrossFade(this.selectedObject)});
        }
        this.displayById[userId].startRun(quick);
    }

    forAllNodeDisplays(toRun) {
        Object.values(this.nodeDataById).forEach( (nodeData) => {
            const nodeDisplay = this.displayById[nodeData.id];
            if (nodeDisplay) {
                toRun(nodeDisplay);
            }
        });
    }

    moveStart(userId, nodeId) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].moveStart(nodeDisplay);
    }

    moveArrive(userId, nodeId) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].moveArrive(nodeDisplay);
    }

    hackerProbeServicesSaga(userId, nodeId) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].hackerProbeServices(nodeDisplay);
    }

    hackerProbeConnections(userId, nodeId) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].hackerProbeConnections(nodeDisplay);
    }
}

const runCanvas = new RunCanvas();
export default runCanvas