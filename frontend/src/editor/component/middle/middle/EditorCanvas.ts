import {fabric} from "fabric";
import {SELECT_NODE} from "../../../EditorActions";
import {assertNotNullUndef} from "../../../../common/Assert";
import NodeDisplay from "../../../../common/canvas/display/NodeDisplay";
import ConnectionDisplay from "../../../../common/canvas/display/ConnectionDisplay";
import {Dispatch} from "redux";
import {Connection} from "../../../reducer/ConnectionsReducer";
import {MoveNodeI, NodeI} from "../../../reducer/NodesReducer";
import {IEvent} from "fabric/fabric-impl";
import {delay} from "../../../../common/Util";
import {sendAddConnection, sendMoveNode} from "../../../server/ServerClient";

interface DisplayNodesById {
    [key: string]: NodeDisplay
}

/**
 * This class provides editor map like actions to the fabric canvas.
 */
class EditorCanvas {

    siteId: string = "";
    nodeDisplayById: DisplayNodesById = {};
    connections: ConnectionDisplay[] = [];
    dispatch: Dispatch | null = null;
    nodeSelected: { data: { id: string } } | null = null;
    canvas: fabric.Canvas | null = null;


    init(dispatch: Dispatch) {
        this.dispatch = dispatch;

        this.canvas = new fabric.Canvas('canvas', {
            width: 607,
            height: 715,
            backgroundColor: "#333333",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.on('object:modified', (event: IEvent<MouseEvent>) => {
            this.canvasObjectModified(event);
        });
        this.canvas.on('selection:created', (event: IEvent<MouseEvent>) => {
            this.canvasObjectSelected(event);
        });
        this.canvas.on('selection:updated', (event: IEvent<MouseEvent>) => {
            this.canvasObjectSelected(event);
        });
        this.canvas.on('selection:cleared', () => {
            this.canvasObjectDeSelected();
        });


        this.canvas.on('object:moving', (event: IEvent<MouseEvent>) => {
            this.movingNode(event.target?.data.id);
        });
        this.canvas.selection = false;
    }

    loadSite(siteState: { id: string, nodes: NodeI[], connections: Connection[] }) {
        this.canvas!.getObjects().forEach((oldObject: any) => {
            this.canvas!.remove(oldObject);
        });

        this.nodeDisplayById = {};
        this.connections = [];
        this.nodeSelected = null;
        this.siteId = siteState.id;

        const {nodes, connections} = siteState;

        nodes.forEach(node => {
            this.addNode(node);
        });

        connections.forEach(connection => {
            this.addConnection(connection);
        });

        this.render();
    }

    addNode(nodeData: NodeI) {
        const nodeDisplay = new NodeDisplay(this.canvas, null, nodeData, false);
        nodeDisplay.show();

        this.nodeDisplayById[nodeData.id] = nodeDisplay;
        this.canvas!.discardActiveObject();
        this.render();

        return nodeDisplay;
    }

    selectNode(nodeId: string) {
        const nodeDisplay = this.nodeDisplayById[nodeId];
        nodeDisplay.select();
        this.nodeSelected = nodeDisplay.nodeIcon;
        this.dispatch!({type: SELECT_NODE, data: nodeId});
    }

    addConnection(connectionData: Connection) {
        let fromDisplay = this.nodeDisplayById[connectionData.fromId];
        let toDisplay = this.nodeDisplayById[connectionData.toId];

        let connectionDisplay = new ConnectionDisplay(this.canvas, null, connectionData, fromDisplay, toDisplay);
        connectionDisplay.show();

        this.connections.push(connectionDisplay);
        this.render();
    }

    render() {
        this.canvas!.renderAll();
    }

    canvasObjectModified(event: IEvent<MouseEvent>) {
        if (!event.target) {
            return;
        }

        const nodeId = event.target.data.id;
        const x = event.target.left!;
        const y = event.target.top!

        sendMoveNode({nodeId, x, y});
    }

    moveNode(action: MoveNodeI) {
        let nodeId = action.nodeId;
        let nodeDisplay = this.nodeDisplayById[nodeId];
        assertNotNullUndef(nodeDisplay, action);

        nodeDisplay.move(action);

        this.movingNode(nodeId);
    }

    movingNode(nodeId?: string) {
        if (!nodeId) {
            return;
        }

        const nodeDisplay = this.nodeDisplayById[nodeId];
        nodeDisplay.moving();

        this.connections.forEach(connection => {
            if (connection.connectionData.fromId === nodeId || connection.connectionData.toId === nodeId) {
                connection.endPointsMoved();
            }
        });

        this.render();
    }

    getNodeSelectedId() {
        let selectedObject = this.canvas!.getActiveObject();
        if (selectedObject && selectedObject.get("type") === "node") {
            return selectedObject.data.id;
        }
        return null;
    }

    canvasObjectSelected(event: IEvent<MouseEvent>) {
        let selectedObjects = event.selected;

        if (!selectedObjects || selectedObjects.length === 0) {
            console.log("Selected nothing?: ");
            return;
        }

        const selectedObject = selectedObjects[0];

        if (!selectedObject.data || !selectedObject.data.id) {
            console.log("somehow selected a non-node: " + selectedObject);
            return;
        }

        if (this.nodeSelected != null && event.e && event.e.ctrlKey) {

            sendAddConnection({
                fromId: this.nodeSelected.data.id,
                toId: selectedObject.data.id
            })
            this.nodeSelected = selectedObject as { data: { id: string } }
        } else {
            this.nodeSelected = selectedObject as { data: { id: string } }
        }

        this.dispatch!({type: SELECT_NODE, data: selectedObject.data.id})
    }

    canvasObjectDeSelected() {
        this.nodeSelected = null;
        delay(() => this.dispatch!({type: SELECT_NODE, data: null}))
    }

    updateNetworkId({nodeId, value}: { nodeId: string, value: string }) {
        const display = this.nodeDisplayById[nodeId];
        display.updateNetworkId(value);
    }
}

const editorCanvas = new EditorCanvas();
export default editorCanvas