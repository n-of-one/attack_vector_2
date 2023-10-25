import {fabric} from "fabric"
import {assertNotNullUndef} from "../../../../common/util/Assert"
import {NodeDisplay, SiteStatus} from "../../../../common/canvas/display/NodeDisplay"
import {ConnectionDisplay} from "../../../../common/canvas/display/ConnectionDisplay"
import {Dispatch} from "redux"
import {Connection} from "../../../reducer/ConnectionsReducer"
import {MoveNodeI, NodeI} from "../../../reducer/NodesReducer"
import {Canvas, IEvent} from "fabric/fabric-impl"
import {delay} from "../../../../common/util/Util"
import {sendAddConnection, sendMoveNode} from "../../../server/EditorServerClient"
import {SELECT_NODE} from "../../../reducer/CurrentNodeIdReducer"
import {DisplayCollection} from "../../../../common/canvas/display/util/DisplayCollection";
import {NodeScanStatus} from "../../../../common/enums/NodeStatus";

export interface LoadSiteData {
    id: string,
    nodes: NodeI[],
    connections: Connection[]
}

class EditorCanvas {

    siteId: string = ""
    nodeDisplays = new DisplayCollection<NodeDisplay>("NodeDisplay")
    connections: ConnectionDisplay[] = []
    dispatch: Dispatch = null as unknown as Dispatch // lateinit
    nodeSelected: NodeDisplay | null = null
    canvas: fabric.Canvas = null as unknown as Canvas // lateinit


    init(dispatch: Dispatch) {
        this.dispatch = dispatch

        this.canvas = new fabric.Canvas('canvas', {
            width: 607,
            height: 715,
            backgroundColor: "#333333",
        })

        fabric.Object.prototype.originX = "center"
        fabric.Object.prototype.originY = 'center'

        this.canvas.on('object:modified', (event: IEvent<MouseEvent>) => {
            this.canvasObjectModified(event)
        })
        this.canvas.on('selection:created', (event: IEvent<MouseEvent>) => {
            this.canvasObjectSelected(event)
        })
        this.canvas.on('selection:updated', (event: IEvent<MouseEvent>) => {
            this.canvasObjectSelected(event)
        })
        this.canvas.on('selection:cleared', () => {
            this.canvasObjectDeSelected()
        })


        this.canvas.on('object:moving', (event: IEvent<MouseEvent>) => {
            this.movingNode(event.target?.data.id)
        })
        this.canvas.selection = false
    }

    loadSite(loadSiteData: LoadSiteData) {
        this.canvas!.getObjects().forEach((oldObject: any) => {
            this.canvas!.remove(oldObject)
        })

        this.siteId = loadSiteData.id

        const {nodes, connections} = loadSiteData

        nodes.forEach(node => {
            this.addNode(node)
        })

        connections.forEach(connection => {
            this.addConnection(connection)
        })

        this.render()
    }

    addNode(nodeDataInput: NodeI) {
        const nodeData = { ...nodeDataInput, status: NodeScanStatus.FULLY_SCANNED_4, hacked: false}
        const nodeDisplay = new NodeDisplay(this.canvas, null, nodeData, true, SiteStatus.SCANNING)
        nodeDisplay.show()

        this.nodeDisplays.add(nodeData.id, nodeDisplay)
        this.canvas!.discardActiveObject()
        this.render()

        return nodeDisplay
    }

    selectNode(nodeId: string) {
        const nodeDisplay = this.nodeDisplays.get(nodeId)
        nodeDisplay.select()
        this.nodeSelected = nodeDisplay
        this.dispatch!({type: SELECT_NODE, data: nodeId})
    }

    addConnection(connectionData: Connection) {
        let fromDisplay = this.nodeDisplays.get(connectionData.fromId)
        let toDisplay = this.nodeDisplays.get(connectionData.toId)

        let connectionDisplay = new ConnectionDisplay(this.canvas, null, connectionData, fromDisplay, toDisplay)
        connectionDisplay.show()

        this.connections.push(connectionDisplay)
        this.render()
    }

    render() {
        if (this.canvas) {
            this.canvas!.renderAll()
        }
    }

    canvasObjectModified(event: IEvent<MouseEvent>) {
        if (!event.target) {
            return
        }

        const nodeId = event.target.data.id
        const x = event.target.left!
        const y = event.target.top!

        sendMoveNode({nodeId, x, y})
    }

    moveNode(action: MoveNodeI) {
        let nodeId = action.nodeId
        let nodeDisplay = this.nodeDisplays.get(nodeId)
        assertNotNullUndef(nodeDisplay, action)

        nodeDisplay.move(action)

        this.movingNode(nodeId)
    }

    movingNode(nodeId?: string) {
        if (!nodeId) {
            return
        }

        const nodeDisplay = this.nodeDisplays.get(nodeId)
        nodeDisplay.moving()

        this.connections.forEach(connection => {
            if (connection.connectionData.fromId === nodeId || connection.connectionData.toId === nodeId) {
                connection.endPointsMoved()
            }
        })

        this.render()
    }

    getNodeSelectedId() {
        let selectedObject = this.canvas!.getActiveObject()
        if (selectedObject && selectedObject.get("type") === "node") {
            return selectedObject.data.id
        }
        return null
    }

    canvasObjectSelected(event: IEvent<MouseEvent>) {
        let selectedObjects = event.selected

        if (!selectedObjects || selectedObjects.length === 0) {
            console.log("Selected nothing? ")
            return
        }
        const selectedObject = selectedObjects[0]

        if (!selectedObject.data || !selectedObject.data.id) {
            console.log("somehow selected a non-node: " + selectedObject)
            return
        }

        const newNodeSelected = this.nodeDisplays.get(selectedObject.data.id)

        if (this.nodeSelected != null && event.e && event.e.ctrlKey) {

            sendAddConnection({
                fromId: this.nodeSelected.id,
                toId: newNodeSelected.id
            })
        }
        this.nodeSelected = newNodeSelected

        this.dispatch!({type: SELECT_NODE, data: newNodeSelected.id})
    }

    canvasObjectDeSelected() {
        this.nodeSelected = null
        delay(() => this.dispatch!({type: SELECT_NODE, data: null}))
    }

    updateNetworkId({nodeId, value}: { nodeId: string, value: string }) {
        const display = this.nodeDisplays.get(nodeId)
        display.updateNetworkId(value)
    }
}

export const editorCanvas = new EditorCanvas()
