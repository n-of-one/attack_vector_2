import {fabric} from "fabric";
import {ADD_CONNECTION, MOVE_NODE, SELECT_NODE} from "../../EditorActions";
import {assertNotNullUndef} from "../../../common/Assert";
import {toType} from "../../../common/enums/NodeTypesNames";

/**
 * This class provides editor map like actions to the fabric canvas.
 */
class EditorCanvas {

    constructor() {
        this.nodesById = {};
        this.connections = [];
        this.dispatch = null;
        this.nodeSelected = null;
    }

    init(dispatch) {
        this.dispatch = dispatch;

        this.canvas = new fabric.Canvas('canvas', {
            width: 607,
            height: 815,
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

        this.nodesById = {};
        this.connections = [];
        this.nodeSelected = null;

        let { nodes, connections } = siteState;

        nodes.forEach( node => {
            this.addNodeWithoutRender(node);
        });

        connections.forEach( connection => {
            this.addConnectionWithoutRender(connection);
        });

        this.render();
    }

    addNodeWithoutRender(action) {
        const status = (action.ice ? "_PROTECTED" : "_FREE");
        const type = toType(action.type);
        const imageId = type.name + status;

        let image = document.getElementById(imageId);

        let nodeData = {
            id: action.id,
            type: action.type
        };

        let node = new fabric.Image(image, {
            left: action.x,
            top: action.y,
            height: image.height,
            width: image.width,
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,

            data: nodeData,
        });

        node.setControlsVisibility({
            mt: false,
            mb: false,
            ml: false,
            mr: false,
            mtr: false
        });

        this.canvas.add(node);
        this.nodesById[action.id] = node;
        this.canvas.deactivateAll();
    }

    addNodeWithRender(action) {
        this.addNodeWithoutRender(action);
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
        let node = this.nodesById[nodeId];
        assertNotNullUndef(node, action);

        node.set({ left: action.x, top: action.y});
        node.setCoords();

        this.movingNode(nodeId);
    }

    movingNode(nodeId) {
        let node = this.nodesById[nodeId];

        this.connections.forEach( connection => {
            if (connection.data.fromId === nodeId) {
                connection.set({x1: node.left, y1: node.top});
                connection.setCoords();
            }
            if (connection.data.toId === nodeId) {
                connection.set({x2: node.left, y2: node.top});
                connection.setCoords();
            }
        });

        this.render();
   }

   getNodeSelectedId() {
        let selectedObject = this.canvas.getActiveObject();
        if (selectedObject && selectedObject.data && selectedObject.get("type") === "image") {
            return selectedObject.data.id;
        }
        return null;
   }

    canvasObjectSelected(event) {
        let selectedObject  =  event.target;
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

    addConnectionWithoutRender(connectionData) {
        let fromNode = this.nodesById[connectionData.fromId];
        let toNode = this.nodesById[connectionData.toId];


        let line = new fabric.Line(
            [fromNode.left, fromNode.top, toNode.left, toNode.top], {
                stroke: "#cccccc",
                strokeWidth: 4,
                strokeDashArray: [5, 5],
                selectable: false,
                hoverCursor: 'default',
            });

        line.data = connectionData;

        this.canvas.add(line);
        this.connections.push(line);
        this.canvas.sendToBack(line);
    }

    addConnectionWithRender(connectionData) {
        this.addConnectionWithoutRender(connectionData);
        this.render();
    }

}

const editorCanvas = new EditorCanvas();
export default editorCanvas