import {fabric} from "fabric";
import {ADD_CONNECTION, MOVE_NODE, SELECT_NODE} from "../../../EditorActions";
import {assertNotNullUndef} from "../../../../common/Assert";
import NodeDisplay from "../../../../common/canvas/display/NodeDisplay";
import ConnectionDisplay from "../../../../common/canvas/display/ConnectionDisplay";

/**
 * This class provides editor map like actions to the fabric canvas.
 */
class EditorCanvas {

    constructor() {
        this.nodeDisplayById = {};
        this.connections = [];
        this.dispatch = null;
        this.nodeSelected = null;
    }

    init(dispatch) {
        this.dispatch = dispatch;

        this.canvas = new fabric.Canvas('canvas', {
            width: 607,
            height: 715,
            backgroundColor: "#333333",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.on('object:modified', (event) => { this.canvasObjectModified(event); });
        this.canvas.on('object:selected', (event) => { this.canvasObjectSelected(event); });
        this.canvas.on('selection:cleared', (event) => { this.canvasObjectDeSelected(event); });


        this.canvas.on('object:moving', (event) => { this.movingNode(event.target.data.id); });
        this.canvas.selection = false;
    }

    loadSite(siteState) {
        let allObjectsArray = this.canvas.getObjects();
        while(allObjectsArray.length !== 0){
            allObjectsArray[0].remove();
        }

        this.nodeDisplayById = {};
        this.connections = [];
        this.nodeSelected = null;

        let { nodes, connections } = siteState;

        nodes.forEach( node => {
            this.addNode(node);
        });

        connections.forEach( connection => {
            this.addConnection(connection);
        });

        this.render();
    }

    addNode(nodeData) {
        const nodeDisplay = new NodeDisplay(this.canvas, null, nodeData, false);
        nodeDisplay.show();

        this.nodeDisplayById[nodeData.id] = nodeDisplay;
        this.canvas.deactivateAll();
        this.render();

        return nodeDisplay;
    }

    selectNode(nodeId) {
        const nodeDisplay = this.nodeDisplayById[nodeId];
        nodeDisplay.select();
        this.nodeSelected = nodeDisplay.nodeIcon;
        this.dispatch({type: SELECT_NODE, data: nodeId});
    }

    addConnection(connectionData) {
        let fromDisplay = this.nodeDisplayById[connectionData.fromId];
        let toDisplay = this.nodeDisplayById[connectionData.toId];

        let connectionDisplay = new ConnectionDisplay(this.canvas, null, connectionData, fromDisplay, toDisplay);
        connectionDisplay.show();

        this.connections.push(connectionDisplay);
        this.render();
    }

    render() {
        this.canvas.renderAll();
    }

    canvasObjectModified(event) {
        this.dispatch({type: MOVE_NODE,
            id: event.target.data.id,
            x: event.target.left,
            y: event.target.top
        });
        // leads to moveNode
    }

    moveNode(action) {
        let nodeId = action.nodeId;
        let nodeDisplay = this.nodeDisplayById[nodeId];
        assertNotNullUndef(nodeDisplay, action);

        nodeDisplay.move(action);

        this.movingNode(nodeId);
    }

    movingNode(nodeId) {
        const nodeDisplay = this.nodeDisplayById[nodeId];
        nodeDisplay.moving();

        this.connections.forEach( connection => {
            if (connection.connectionData.fromId === nodeId || connection.connectionData.toId === nodeId) {
                connection.endPointsMoved();
            }
        });

        this.render();
   }

   getNodeSelectedId() {
        let selectedObject = this.canvas.getActiveObject();
        if (selectedObject && selectedObject.get("type") === "node") {
            return selectedObject.data.id;
        }
        return null;
   }

    canvasObjectSelected(event) {
        let selectedObject  =  event.target;

        if (!selectedObject.data || !selectedObject.data.id) {
            console.log("somehow selected a non-node: " + selectedObject);
            return;
        }

        if (this.nodeSelected && event.e && event.e.ctrlKey) {
            this.dispatch({type: ADD_CONNECTION, fromId: this.nodeSelected.data.id, toId: selectedObject.data.id });
            this.nodeSelected = selectedObject;
        }
        else {
            this.nodeSelected = selectedObject;
        }

        this.dispatch({type: SELECT_NODE, data: selectedObject.data.id});
    }

    canvasObjectDeSelected(event) {
        this.nodeSelected = null;
        this.dispatch({type: SELECT_NODE, data: null});
    }

    updateNetworkId({nodeId, value}) {
        const display = this.nodeDisplayById[nodeId];
        display.updateNetworkId(value);
    }

    openLayer(nodeId, layerId) {
        this.selectNode(nodeId);
    }
}

const editorCanvas = new EditorCanvas();
export default editorCanvas