import {fabric} from "fabric"
import {Schedule} from "../../../common/util/Schedule"
import {NodeScanStatus, UNDISCOVERED_0} from "../../../common/enums/NodeStatus"
import {NodeDisplay} from "../../../common/canvas/display/NodeDisplay"
import {HackerDisplay} from "../../../common/canvas/display/HackerDisplay"
import {ProbeDisplay} from "../../../common/canvas/display/ProbeDisplay"
import {CANVAS_HEIGHT, CANVAS_WIDTH} from "../../../common/canvas/CanvasConst"
import {DISPLAY_NODE_INFO, HIDE_NODE_INFO} from "../model/ScanActions"
import {TracingPatrollerDisplay} from "../../../common/canvas/display/TracingPatrollerDisplay"
import {Dispatch} from "redux"
import {Canvas, IEvent} from "fabric/fabric-impl"
import {HackerPresence} from "../reducer/HackersReducer"
import {LayerDetails, NodeI} from "../../../editor/reducer/NodesReducer"
import {Connection} from "../../../editor/reducer/ConnectionsReducer"
import {ConnectionDisplay} from "../../../common/canvas/display/ConnectionDisplay";
import {DisplayCollection} from "../../../common/canvas/display/util/DisplayCollection";
import {
    ActionPatrollerCatchesHacker,
    ActionPatrollerMove,
    HackerScansNodeAction,
    MoveArriveAction, MoveArriveFailAction,
    MoveStartAction, NodeStatusById,
    PatrollerData,
    ProbeAction,
    SiteAndScan
} from "../../server/RunServerActionProcessor";
import {ProbeVisual} from "../../../common/canvas/visuals/ProbeVisual";
import {Timings} from "../../../common/model/Ticks";


export type NodeScanType = "SCAN_NODE_INITIAL" | "SCAN_CONNECTIONS" | "SCAN_NODE_DEEP"

/// Probe displays need a unique ID, but this ID only exists in the browser.
let probeDisplayIdSequence = 0




/// This class renders the sit map on the JFabric Canvas
class RunCanvas {
    nodeDataById: { [key: string]: NodeI } = {}
    connectionDataById: { [key: string]: Connection } = {}
    hackers: HackerPresence[] = []


    nodeDisplays = new DisplayCollection<NodeDisplay>("NodeDisplay")
    hackerDisplays = new DisplayCollection<HackerDisplay>("HackerDisplay")
    connectionDisplays = new DisplayCollection<ConnectionDisplay>("ConnectionDisplay")
    probeDisplays = new DisplayCollection<ProbeDisplay>("ProbeDisplay")
    patrollerDisplays = new DisplayCollection<TracingPatrollerDisplay>("TracingPatrollerDisplay")

    dispatch: Dispatch = null as unknown as Dispatch // lateinit
    iconSchedule: Schedule = null as unknown as Schedule // lateinit

    startNodeDisplay: NodeDisplay = null as unknown as NodeDisplay // lateinit
    userId: string = null as unknown as string

    canvas: Canvas = null as unknown as Canvas // lateinit
    selectedObject: fabric.Image | null = null
    hacking = false


    init(userId: string, dispatch: Dispatch) {

        console.log("Runcanvas.init")

        this.userId = userId
        this.dispatch = dispatch

        this.canvas = new fabric.Canvas('runCanvas', {
            width: CANVAS_WIDTH,
            height: CANVAS_HEIGHT,
            backgroundColor: "#333333",
        })

        fabric.Object.prototype.originX = "center"
        fabric.Object.prototype.originY = 'center'

        this.canvas.selection = false

        this.canvas.on('selection:created', (event) => {
            this.canvasObjectSelected(event)
        })
        this.canvas.on('selection:updated', (event) => {
            this.canvasObjectSelected(event)
        })
        this.canvas.on('selection:cleared', () => {
            this.canvasObjectDeSelected()
        })

        this.iconSchedule = new Schedule(dispatch)
    }

    reset() {
        console.log("Runcanvas.reset")

        if (this.canvas === null) {
            return
        }

        this.nodeDisplays.removeAllAndTerminate(this.canvas)
        this.hackerDisplays.removeAllAndTerminate(this.canvas)
        this.connectionDisplays.removeAllAndTerminate(this.canvas)
        this.probeDisplays.removeAllAndTerminate(this.canvas)

        this.selectedObject = null

        this.nodeDataById = {}
        this.hackers = []


        this.connectionDataById = {}
        this.startNodeDisplay = null as unknown as NodeDisplay

        this.iconSchedule.terminate()
        this.iconSchedule = new Schedule(this.dispatch)
        this.hacking = false

        this.render()
    }

    render() {
        this.canvas.renderAll()
    }

    loadScan(actionData: SiteAndScan) {

        console.log("Runcanvas.loadScan")


        const scan = structuredClone(actionData.run)
        const site = structuredClone(actionData.site)
        const hackers = structuredClone(actionData.hackers)
        const patrollers = structuredClone(actionData.patrollers)

        const nodes = site.nodes
        const connections = site.connections

        this.nodeDataById = {}
        this.sortAndAddHackers(hackers)

        /* Similarly the layer status is added to each layer. */
        const layerById: { [key: string]: LayerDetails } = {}
        nodes.forEach((node) => {
            node.layers.forEach((layer) => {
                layerById[layer.id] = layer
            })
        })

        nodes.forEach((nodeData: NodeI) => {
            this.nodeDataById[nodeData.id] = nodeData
            const nodeScan = scan.nodeScanById[nodeData.id]
            nodeData.status = nodeScan.status
            nodeData.distance = nodeScan.distance
        })
        nodes.forEach(node => {
            if (node.status !== UNDISCOVERED_0) {
                this.addNodeDisplay(node)
            }
        })

        this.connectionDataById = {}
        connections.forEach((connectionData) => {
            this.connectionDataById[connectionData.id] = connectionData
        })
        connections.forEach(connection => {

            const fromDisplay = this.nodeDisplays.getOrNull(connection.fromId)
            const toDisplay = this.nodeDisplays.getOrNull(connection.toId)
            if (fromDisplay && toDisplay) {
                this.addConnectionDisplay(connection, fromDisplay, toDisplay)
            }
        })

        this.startNodeDisplay = this.nodeDisplays.get(nodes[0].id)
        this.addHackersDisplays()

        patrollers.forEach((patrollerData) => {
            this.activateTracingPatroller(patrollerData)
        })
    }

    sortAndAddHackers(hackers: HackerPresence[]) {
        const you = hackers.find(hacker => hacker.userId === this.userId)!
        const others = hackers.filter(hacker => hacker.userId !== this.userId)
        others.sort((a, b) => (a.userName > b.userName) ? 1 : ((b.userName > a.userName) ? -1 : 0))
        const midIndex = Math.floor(others.length / 2)
        this.hackers = others
        this.hackers.splice(midIndex, 0, you)
    }

    addHackersDisplays() {
        const step = Math.floor(CANVAS_WIDTH / (this.hackers.length + 1))
        this.hackers.forEach((hacker, index) => {
            this.addHackerDisplay(hacker, step * (index + 1))
        })
    }

    addHackerDisplay(hacker: HackerPresence, offset: number) {
        const you = hacker.userId === this.userId
        const hackerDisplay = new HackerDisplay(this.canvas, this.startNodeDisplay, hacker, offset, you, this.dispatch, this.nodeDisplays)
        this.hackerDisplays.add(hacker.userId, hackerDisplay)
    }

    removeHackerDisplay(leavingHackerUserId: string) {
        const hackerDisplay = this.hackerDisplays.get(leavingHackerUserId)
        this.hackerDisplays.remove(leavingHackerUserId)
        hackerDisplay.disappear()
    }

    addNodeDisplay(node: NodeI) {
        const nodeDisplay = new NodeDisplay(this.canvas, this.iconSchedule, node, true, this.hacking)
        this.nodeDisplays.add(node.id, nodeDisplay)
        nodeDisplay.appear()
    }

    addConnectionDisplay(connection: Connection, fromDisplay: NodeDisplay, toDisplay: NodeDisplay) {
        const connectionDisplay = new ConnectionDisplay(this.canvas, this.iconSchedule, connection, fromDisplay, toDisplay)
        this.connectionDisplays.add(connection.id, connectionDisplay)
        connectionDisplay.appear()
    }

    launchProbe(probeAction: ProbeAction) {
        const hackerDisplay = this.hackerDisplays.get(probeAction.probeUserId)
        if (!hackerDisplay) return
        const yourProbe = probeAction.probeUserId === this.userId

        const probeId = "probe-" + probeDisplayIdSequence++;
        const probeDisplay = new ProbeDisplay(this.canvas, probeAction.path, probeAction.scanType,
            hackerDisplay, yourProbe, this.nodeDisplays, probeAction.timings)
        this.probeDisplays.add(probeId, probeDisplay)
    }

    updateNodeStatus(nodeId: string, status: NodeScanStatus) {
        if (this.nodeDisplays.has(nodeId)) {
            this.nodeDisplays.get(nodeId).updateStatus(status, this.selectedObject)
        } else {
            const nodeData = this.nodeDataById[nodeId]
            nodeData.status = status
            this.addNodeDisplay(nodeData)
        }
    }

    discoverNodes(nodeStatusById: NodeStatusById, connectionIds: string[]) {
        Object.entries(nodeStatusById).forEach(([nodeId, status]) => {
            this.updateNodeStatus(nodeId, status)
        })

        connectionIds.forEach((id) => {
            if (!this.connectionDisplays.has(id)) {
                const connection = this.connectionDataById[id]
                const fromIcon = this.nodeDisplays.get(connection.fromId)
                const toIcon = this.nodeDisplays.get(connection.toId)
                this.addConnectionDisplay(connection, fromIcon, toIcon)
            }
        })
    }

    hackerEnter(newHacker: HackerPresence) {
        if (newHacker.userId === this.userId) {
            return
        }
        if (this.hackers.length % 2 === 0) {
            // Even number of hackers: hacker gets added to the left.
            this.hackers.splice(0, 0, newHacker)
        } else {
            // Odd: add to right.
            this.hackers.push(newHacker)
        }
        this.repositionHackers(newHacker.userId)
    }

    hackerLeave(leavingHackerUserId: string) {
        if (leavingHackerUserId === this.userId) {
            return
        }
        this.removeHackerDisplay(leavingHackerUserId)
        this.hackers = this.hackers.filter(element => element.userId !== leavingHackerUserId)
        this.repositionHackers(leavingHackerUserId)
    }

    repositionHackers(targetHackerUserId: string) {
        const step = Math.floor(CANVAS_WIDTH / (this.hackers.length + 1))
        this.hackers.forEach((hacker, index) => {
            const newX = step * (index + 1)
            if (hacker.userId === targetHackerUserId) {
                this.addHackerDisplay(hacker, newX)
            } else {
                this.hackerDisplays.get(hacker.userId).repositionHackerIdentification(newX)
            }
        })
    }

    canvasObjectSelected(event: IEvent<MouseEvent>) {
        let selectedObjects = event.selected
        if (!selectedObjects || selectedObjects.length === 0) {
            return
        }
        const selectedObject = selectedObjects[0]

        if (selectedObject.type === "node" && selectedObject instanceof fabric.Image) {
            this.selectedObject = selectedObject
            this.dispatch({type: DISPLAY_NODE_INFO, nodeId: selectedObject.data.id})
        }
    }

    canvasObjectDeSelected() {
        this.selectedObject = null
        this.dispatch({type: HIDE_NODE_INFO})
    }

    unSelect() {
        this.canvas.discardActiveObject()
        this.canvas.requestRenderAll()
        this.canvasObjectDeSelected()
    }

    // TODO consider retyping string to UserIdType
    startAttack(userId: string, quick: boolean, timings: Timings) {
        this.hacking = true
        if (this.userId === userId) {
            if (!quick) {
                this.iconSchedule.wait(30)
            }

            this.nodeDisplays.forEach((nodeDisplay: NodeDisplay) => {
                nodeDisplay.transitionToHack(quick)
            })
            this.nodeDisplays.forEach((nodeDisplay: NodeDisplay) => {
                nodeDisplay.cleanUpAfterCrossFade(this.selectedObject)
            })
        }
        this.hackerDisplays.get(userId).startRun(quick, timings)
    }


    moveStart({userId, nodeId, timings}: MoveStartAction) {
        const nodeDisplay = this.nodeDisplays.get(nodeId)
        this.hackerDisplays.get(userId).moveStart(nodeDisplay, timings)
    }

    moveArrive({userId, nodeId, timings}: MoveArriveAction) {
        const nodeDisplay = this.nodeDisplays.get(nodeId)
        this.hackerDisplays.get(userId).moveArrive(nodeDisplay, timings)
    }

    moveArriveFail(data: MoveArriveFailAction) {
        this.hackerDisplays.get(data.userId).moveArriveFail()
    }

    hackerScansNode({userId, nodeId, timings}: HackerScansNodeAction) {
        const nodeDisplay = this.nodeDisplays.get(nodeId)
        const probe = new ProbeVisual(this.canvas, nodeDisplay, new Schedule(this.dispatch))
        probe.zoomInAndOutAndRemove(timings)
    }

    nodeHacked(nodeId: string) {
        this.nodeDataById[nodeId].hacked = true
        this.nodeDisplays.get(nodeId).hacked()
    }

    stop() {
        this.iconSchedule.terminate()
        this.nodeDisplays.removeAllAndTerminate(this.canvas)
        this.hackerDisplays.removeAllAndTerminate(this.canvas)
        this.connectionDisplays.removeAllAndTerminate(this.canvas)
        this.probeDisplays.removeAllAndTerminate(this.canvas)
    }

    flashTracingPatroller(nodeId: string) {
        const patrollerData = {
            patrollerId: null, nodeId, timings: {appear: 20}, path: []
        }
        new TracingPatrollerDisplay(patrollerData, this.canvas, this.dispatch, this.nodeDisplays, this.hackerDisplays).disappear()
    }

    activateTracingPatroller(patrollerData: PatrollerData) {
        const patrollerDisplay = new TracingPatrollerDisplay(patrollerData, this.canvas, this.dispatch, this.nodeDisplays, this.hackerDisplays)
        this.patrollerDisplays.add(patrollerData.patrollerId!, patrollerDisplay)
    }

    patrollerLocksHacker({patrollerId, hackerId}: ActionPatrollerCatchesHacker) {
        const patroller = this.patrollerDisplays.get(patrollerId)
        patroller.lock(hackerId)
    }

    movePatroller({patrollerId, fromNodeId, toNodeId, timings}: ActionPatrollerMove) {
        this.patrollerDisplays.get(patrollerId).move(fromNodeId, toNodeId, timings)
    }

    removePatroller(patrollerId: string) {
        this.patrollerDisplays.get(patrollerId).disappear()
        this.patrollerDisplays.remove(patrollerId)
    }


}

export const runCanvas = new RunCanvas()
