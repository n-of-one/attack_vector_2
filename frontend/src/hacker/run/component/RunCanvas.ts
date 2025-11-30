import {fabric} from "fabric"
import {Schedule} from "../../../common/util/Schedule"
import {NodeScanStatus} from "../../../common/enums/NodeStatus"
import {NodeDisplay, SiteStatus} from "../../../common/canvas/display/NodeDisplay"
import {HackerDisplay} from "../../../common/canvas/display/HackerDisplay"
import {ProbeDisplay} from "../../../common/canvas/display/ProbeDisplay"
import {CANVAS_HEIGHT, CANVAS_WIDTH} from "../../../common/canvas/CanvasConst"
import {Dispatch} from "redux"
import {Canvas, IEvent} from "fabric/fabric-impl"
import {HackerPresence} from "../reducer/HackerPresencesReducer"
import {Connection} from "../../../editor/reducer/ConnectionsReducer"
import {ConnectionDisplay} from "../../../common/canvas/display/ConnectionDisplay";
import {DisplayCollection} from "../../../common/canvas/display/util/DisplayCollection";
import {HackerScansNodeAction, MoveArriveAction, MoveStartAction, NodeStatusById, ProbeAction, SiteAndScan} from "../../RunServerActionProcessor";
import {ProbeVisual} from "../../../common/canvas/visuals/ProbeVisual";
import {Timings} from "../../../common/model/Ticks";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {TracingPatrollerDisplay} from "../../../common/canvas/display/TracingPatrollerDisplay";
import {DISPLAY_NODE_INFO, HIDE_NODE_INFO} from "../reducer/InfoNodeIdReducer";
import {LayerDetails, NodeI} from "../../../common/sites/SiteModel";
import {APPEAR_ANIMATION_TICKS, NODE_TRANSITION_TICKS} from "../../../common/canvas/display/DisplayAnimationConstants";


/// Probe displays need a unique ID, but this ID only exists in the browser.
let probeDisplayIdSequence = 0

/// This class renders the sit map on the JFabric Canvas
class RunCanvas {

    active = false // active defines if the user is actually seeing the RunCanvas. If not, we don't want to process updates.

    nodeDataById: { [key: string]: NodeI } = {}
    connectionDataById: { [key: string]: Connection } = {}
    hackers: HackerPresence[] = []


    nodeDisplays = new DisplayCollection<NodeDisplay>("NodeDisplay")
    hackerDisplays = new DisplayCollection<HackerDisplay>("HackerDisplay")
    connectionDisplays = new DisplayCollection<ConnectionDisplay>("ConnectionDisplay")
    probeDisplays = new DisplayCollection<ProbeDisplay>("ProbeDisplay")

    dispatch: Dispatch = null as unknown as Dispatch // lateinit
    iconSchedule: Schedule = null as unknown as Schedule // lateinit

    startNodeDisplay: NodeDisplay = null as unknown as NodeDisplay // lateinit
    userId: string = null as unknown as string
    siteId: string | null = null
    runId: string | null = null

    canvas: Canvas = null as unknown as Canvas // lateinit
    selectedObject: fabric.Image | null = null
    hacking = false
    shutdown = false


    init(userId: string, dispatch: Dispatch) {

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

    clearState() {
        if (this.canvas === null) {
            throw Error("RunCanvas.reset() called before init()")
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

        this.runId = null
        this.siteId = null

        this.hacking = false
        this.shutdown = false

        this.render()
    }

    render() {
        this.canvas.renderAll()
    }

    enterSite(actionData: SiteAndScan) {
        this.clearState()

        this.siteId = actionData.site.id
        this.runId = actionData.run.runId

        const scan = structuredClone(actionData.run)
        const site = structuredClone(actionData.site)
        const hackers = structuredClone(actionData.hackers)

        const nodes = site.nodes
        const connections = site.connections

        this.shutdown = site.siteProperties.shutdownEnd !== null

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
            if (node.status !== NodeScanStatus.UNDISCOVERED_0) {
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

        this.scheduleIconAnimationsFinishedEvent(APPEAR_ANIMATION_TICKS , "enterSiteAnimationsFinished")
        this.active = true
    }

    // Inform Playwright that the animations have finished
    private scheduleIconAnimationsFinishedEvent(animationTicks: number, eventName: string) {
        this.iconSchedule.wait(animationTicks + 1)
        this.iconSchedule.run(0, () => {
            window.dispatchEvent(new CustomEvent(eventName));
        })
    }

    private sortAndAddHackers(hackers: HackerPresence[]) {
        const you = hackers.find(hacker => hacker.userId === this.userId)!
        const others = hackers.filter(hacker => hacker.userId !== this.userId)
        others.sort((a, b) => (a.userName > b.userName) ? 1 : ((b.userName > a.userName) ? -1 : 0))
        const midIndex = Math.floor(others.length / 2)
        this.hackers = others
        this.hackers.splice(midIndex, 0, you)
    }

    private addHackersDisplays() {
        const step = Math.floor(CANVAS_WIDTH / (this.hackers.length + 1))
        this.hackers.forEach((hacker, index) => {
            this.addHackerDisplay(hacker, step * (index + 1))
        })
    }

    private addHackerDisplay(hacker: HackerPresence, offset: number) {
        const you = hacker.userId === this.userId
        const hackerDisplay = new HackerDisplay(this.canvas, this.startNodeDisplay, hacker, offset, you, this.dispatch, this.nodeDisplays)
        this.hackerDisplays.add(hacker.userId, hackerDisplay)
    }


    private addNodeDisplay(node: NodeI) {
        const nodeSiteStatus = this.determineNodeSiteStatus(this.hacking, this.shutdown)

        const nodeDisplay = new NodeDisplay(this.canvas, this.iconSchedule, node, false, nodeSiteStatus)
        this.nodeDisplays.add(node.id, nodeDisplay)
        nodeDisplay.appear()
    }

    private determineNodeSiteStatus(hacking: boolean, shutdown: boolean): SiteStatus {
        if (this.shutdown) return SiteStatus.SHUTDOWN
        return (this.hacking) ? SiteStatus.INSIDE : SiteStatus.OUTSIDE
    }


    private addConnectionDisplay(connection: Connection, fromDisplay: NodeDisplay, toDisplay: NodeDisplay) {
        const connectionDisplay = new ConnectionDisplay(this.canvas, this.iconSchedule, connection, fromDisplay, toDisplay)
        this.connectionDisplays.add(connection.id, connectionDisplay)
        connectionDisplay.appear()
    }

    launchProbe(probeAction: ProbeAction) {
        if (!this.active) return

        const hackerDisplay = this.hackerDisplays.get(probeAction.probeUserId)
        if (!hackerDisplay) return
        const yourProbe = probeAction.probeUserId === this.userId

        const probeId = "probe-" + probeDisplayIdSequence++;
        const probeDisplay = new ProbeDisplay(this.canvas, probeAction.path, probeAction.scanType,
            hackerDisplay, yourProbe, this.nodeDisplays, probeAction.timings)
        this.probeDisplays.add(probeId, probeDisplay)
    }

    updateNodeStatus(nodeId: string, status: NodeScanStatus) {
        if (!this.active) return

        if (this.nodeDisplays.has(nodeId)) {
            this.nodeDisplays.get(nodeId).updateStatus(status, this.selectedObject)

            return false // already displayed
        } else {
            const nodeData = this.nodeDataById[nodeId]
            nodeData.status = status
            this.addNodeDisplay(nodeData)
            return true // new node to display
        }
    }

    discoverNodes(nodeStatusById: NodeStatusById) {
        if (!this.active) return

        const newNodes: string[] = []
        Object.entries(nodeStatusById).forEach(([nodeId, status]) => {
            if (this.updateNodeStatus(nodeId, status)) {
                newNodes.push(nodeId)
            }
        })

        let connectionIds = new Set<string>()
        newNodes.forEach((nodeId) => {
            this.findConnections(nodeId).forEach((connectionId) => {
                connectionIds.add(connectionId)
            })
        })

        connectionIds.forEach((id) => {
            if (!this.connectionDisplays.has(id)) {
                const connection = this.connectionDataById[id]
                const fromIcon = this.nodeDisplays.getOrNull(connection.fromId)
                const toIcon = this.nodeDisplays.getOrNull(connection.toId)
                if (fromIcon && toIcon) {
                    this.addConnectionDisplay(connection, fromIcon, toIcon)
                }
            }
        })

        this.scheduleIconAnimationsFinishedEvent(APPEAR_ANIMATION_TICKS, "scanAnimationsFinished")
    }

    findConnections(nodeId: string): string[] {
        const connectionsForNode: string[] = []
        Object.entries(this.connectionDataById).forEach(([connectionId, connection]) => {
            if (connection.fromId === nodeId || connection.toId === nodeId) {
                connectionsForNode.push(connectionId)
            }
        })
        return connectionsForNode
    }

    hackerEnter(newHacker: HackerPresence) {
        if (!this.active) return

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

    startAttack(userId: string, quick: boolean, timings: Timings) {
        if (!this.active) return

        if (this.userId === userId) {
            this.hacking = true
            if (!quick) {
                this.iconSchedule.wait(30)
            }

            this.nodeDisplays.forEach((nodeDisplay: NodeDisplay) => {
                nodeDisplay.transitionToInside(quick, this.selectedObject)
            })
        }
        this.hackerDisplays.get(userId).startRun(quick, timings)

        const waitTime = quick ? NODE_TRANSITION_TICKS : timings.main - 50
        this.scheduleIconAnimationsFinishedEvent(waitTime , "startAttackAnimationsFinished")
    }

    // Called as a consequence of SERVER_HACKER_LEAVE_SCAN, which is a consequence of MenuItem calling: /run/leaveRun
    hackerLeave(leavingHackerUserId: string) {
        if (!this.active) return

        if (leavingHackerUserId === this.userId) {
            webSocketConnection.unsubscribeForRun(this.runId, this.siteId)
            this.clearState()
            this.active = false
            return
        }
        this.removeHackerDisplay(leavingHackerUserId)
        this.hackers = this.hackers.filter(element => element.userId !== leavingHackerUserId)
        this.repositionHackers(leavingHackerUserId)
    }

    private removeHackerDisplay(leavingHackerUserId: string) {
        const hackerDisplay = this.hackerDisplays.get(leavingHackerUserId)
        this.hackerDisplays.remove(leavingHackerUserId)
        hackerDisplay.disappear()
    }


    moveStart({userId, nodeId, timings, bypassingIceAtStartNode}: MoveStartAction) {
        if (!this.active) return

        const nodeDisplay = this.nodeDisplays.get(nodeId)
        this.hackerDisplays.get(userId).moveStart(nodeDisplay, bypassingIceAtStartNode, timings)
    }

    moveArrive({userId, nodeId, timings}: MoveArriveAction) {
        if (!this.active) return

        const nodeDisplay = this.nodeDisplays.get(nodeId)
        this.hackerDisplays.get(userId).moveArrive(nodeDisplay, timings)
    }

    hackerScansNode({userId, nodeId, timings}: HackerScansNodeAction) {
        if (!this.active) return

        const nodeDisplay = this.nodeDisplays.get(nodeId)
        const probe = new ProbeVisual(this.canvas, nodeDisplay, new Schedule(this.dispatch))
        probe.zoomInAndOutAndRemove(timings)
    }

    nodeHacked(nodeId: string) {
        if (!this.active) return

        this.nodeDataById[nodeId].hacked = true
        this.nodeDisplays.get(nodeId).hacked(this.selectedObject)

        this.scheduleIconAnimationsFinishedEvent(NODE_TRANSITION_TICKS , "nodeHackedAnimationFinished")
    }

    stop() {
        this.iconSchedule.terminate()
        this.nodeDisplays.removeAllAndTerminate(this.canvas)
        this.hackerDisplays.removeAllAndTerminate(this.canvas)
        this.connectionDisplays.removeAllAndTerminate(this.canvas)
        this.probeDisplays.removeAllAndTerminate(this.canvas)
    }

    flashTracingPatroller(nodeId: string) {
        if (!this.active) return

        const patrollerData = {
            patrollerId: null, nodeId, timings: {appear: 20}, path: []
        }
        new TracingPatrollerDisplay(patrollerData, this.canvas, this.dispatch, this.nodeDisplays, this.hackerDisplays).disappear()
    }

    disconnect(userId: string) {
        if (!this.active) return

        if (userId === this.userId) {
            this.nodeDisplays.forEach((nodeDisplay: NodeDisplay) => {
                nodeDisplay.transitionToOutside()
            })
            this.iconSchedule.wait(20)
        }

        this.hackerDisplays.get(userId).disconnect()
    }

    siteShutdownStart() {
        this.nodeDisplays.forEach((nodeDisplay: NodeDisplay) => {
            nodeDisplay.siteShutdownStart()
        })
    }

    siteShutdownFinish() {
        this.nodeDisplays.forEach((nodeDisplay: NodeDisplay) => {
            nodeDisplay.siteShutdownFinish()
        })
    }
}

export const runCanvas = new RunCanvas()
