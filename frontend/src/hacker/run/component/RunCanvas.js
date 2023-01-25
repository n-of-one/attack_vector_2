import {fabric} from "fabric";
import Schedule from "../../../common/Schedule";
import {DISCOVERED, UNDISCOVERED} from "../../../common/enums/NodeStatus";
import NodeDisplay from "../../../common/canvas/display/NodeDisplay";
import ConnectionIcon from "../../../common/canvas/display/ConnectionDisplay";
import HackerIcon from "../../../common/canvas/display/HackerDisplay";
import ProbeDisplay from "../../../common/canvas/display/ProbeDisplay";
import {CANVAS_HEIGHT, CANVAS_WIDTH} from "../../../common/canvas/CanvasConst";
import {DISPLAY_NODE_INFO, HIDE_NODE_INFO} from "../model/ScanActions";
import TracingPatrollerDisplay from "../../../common/canvas/display/TracingPatrollerDisplay";

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

        this.canvas.on('selection:created', (event) => { this.canvasObjectSelected(event); });
        this.canvas.on('selection:updated', (event) => { this.canvasObjectSelected(event); });
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

        this.iconSchedule.terminate();
        this.iconSchedule = new Schedule();
        this.hacking = false;

        this.render();
    }

    render() {
        this.canvas.renderAll();
    }

    loadScan(actionData) {
        const scan = structuredClone(actionData.scan)
        const site = structuredClone(actionData.site)
        const hackers = structuredClone(actionData.hackers)
        const patrollers = structuredClone(actionData.patrollers)

        const nodes = site.nodes
        const connections = site.connections
        const nodeStatuses = site.nodeStatuses
        const layerStatuses = site.layerStatuses

        this.nodeDataById = {};
        this.sortAndAddHackers(hackers);

        /* On the server side, the node status is tied to a run. But client side we always are in one run.
        So the node Status (per run) is added to the node itself to make it simpler. */
        nodeStatuses.forEach((nodeStatus) => {
            const node = nodes.find((node) => node.id === nodeStatus.nodeId);
            node.hacked = nodeStatus.hacked;
        });

        /* Similarly the layer status is added to each layer. */
        const layerById = {};
        nodes.forEach((node) => {
            node.layers.forEach((layer) => {
                layerById[layer.id] = layer;
            });
        });
        layerStatuses.forEach((layerStatus) => {
            const layer = layerById[layerStatus.layerId];
            layer.hacked = layerStatus.hacked;
        });


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

        patrollers.forEach((patrollerData) => {this.activateTracingPatroller(patrollerData)});
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
        this.displayById[hacker.userId] = new HackerIcon(this.canvas, this.startNodeDisplay, hacker, offset, you, this.dispatch, this.displayById);
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
        let selectedObjects  =  event.selected;
        if (!selectedObjects || selectedObjects.length === 0) {
            return;
        }
        const selectedObject = selectedObjects[0];

        if (selectedObject.type === "node") {
            this.selectedObject = selectedObject;
            this.dispatch({type: DISPLAY_NODE_INFO, nodeId: selectedObject.data.id});
        }
    }

    canvasObjectDeSelected() {
        this.selectedObject = null;
        this.dispatch({type: HIDE_NODE_INFO});
    }

    unSelect() {
        this.canvas.discardActiveObject();
        this.canvas.requestRenderAll();
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

    moveStart({userId, nodeId, ticks}) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].moveStart(nodeDisplay, ticks);
    }

    moveArrive(userId, nodeId) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].moveArrive(nodeDisplay);
    }

    hackerProbeLayersSaga({userId, ticks}) {
        this.displayById[userId].hackerProbeLayers(ticks);
    }

    hackerProbeConnections(userId, nodeId) {
        const nodeDisplay = this.displayById[nodeId];
        this.displayById[userId].hackerProbeConnections(nodeDisplay);
    }

    nodeHacked(nodeId) {
        this.nodeDataById[nodeId].hacked = true;
        this.displayById[nodeId].hacked();
    }

    stop() {
        this.iconSchedule.terminate();
        Object.values(this.displayById).forEach( (display) => display.terminate() );
    }

    flashTracingPatroller({nodeId}) {
        const patrollerData = {
            patrollerId: null, nodeId, ticks: {appear: 20 }
        };
        new TracingPatrollerDisplay(patrollerData, this.canvas, null, this.displayById).disappear(20);
    }

    activateTracingPatroller(patrollerData) {
        this.displayById[patrollerData.patrollerId] = new TracingPatrollerDisplay(patrollerData, this.canvas, this.dispatch, this.displayById);
    }


    patrollerHooksHacker({hackerId}) {
        this.displayById[hackerId].hookByPatroller();
    }

    patrollerSnacksBackHacker({hackerId, ticks}) {
        this.displayById[hackerId].snapBack(ticks);
    }

    patrollerLocksHacker({patrollerId, hackerId}) {
        const patroller = this.displayById[patrollerId];
        patroller.lock(hackerId);
    }

    movePatroller({patrollerId, fromNodeId, toNodeId, ticks}) {
        this.displayById[patrollerId].move(fromNodeId, toNodeId, ticks);
    }

    removePatroller({patrollerId}) {
        this.displayById[patrollerId].disappear();
        delete this.displayById[patrollerId];
    }
}

const runCanvas = new RunCanvas();
export default runCanvas