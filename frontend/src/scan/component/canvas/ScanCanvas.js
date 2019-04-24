import {fabric} from "fabric";
import {ADD_CONNECTION, MOVE_NODE, SELECT_NODE} from "../../../editor/EditorActions";
import {assertNotNullUndef} from "../../../common/Assert";
import {toType} from "../../../common/NodeTypesNames";
import Thread from "../../../common/Thread";

/**
 * This class renders the scan map on the JFabric Canvas
 */
class ScanCanvas {



    nodesById = {};
    connections = [];
    dispatch = null;
    thread = new Thread();

    init(dispatch) {
        this.dispatch = dispatch;

        this.canvas = new fabric.StaticCanvas('scanCanvas', {
            width: 607,
            height: 815,
            backgroundColor: "#333333",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        // this.canvas.on('object:moving', (event) => { this.movingNode(event.target.data.id); });
        this.canvas.selection = false;
    }

    loadScan(data) {
        let {site} = data;
        // let {scan, site} = data;
        let allObjectsArray = this.canvas.getObjects();
        while(allObjectsArray.length !== 0){
            allObjectsArray[0].remove();
        }

        this.nodesById = {};
        this.connections = [];

        let { nodes, connections } = site;

        nodes.forEach( node => {
            this.addNodeWithAnimation(node);
        });

        connections.forEach( connection => {
            this.addConnectionWithAnimation(connection);
        });
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
            opacity: 0,

            data: nodeData,
        });

        this.canvas.add(node);
        this.nodesById[action.id] = node;

        return node;
    }

    render() {
        this.canvas.renderAll();
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
            if (connection.data.from === nodeId) {
                connection.set({x1: node.left, y1: node.top});
                connection.setCoords();
            }
            if (connection.data.to === nodeId) {
                connection.set({x2: node.left, y2: node.top});
                connection.setCoords();
            }
        });

        this.render();
   }

    addConnectionWithoutRender(connectionData) {
        let fromNode = this.nodesById[connectionData.from];
        let toNode = this.nodesById[connectionData.to];

        let line = new fabric.Line(
            [fromNode.left, fromNode.top, toNode.left, toNode.top], {
                stroke: "#cccccc",
                strokeWidth: 2,
                strokeDashArray: [3, 3],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });

        line.data = connectionData;

        this.canvas.add(line);
        this.connections.push(line);
        this.canvas.sendToBack(line);

        return line;
    }

    addNodeWithAnimation(action) {
        const node = this.addNodeWithoutRender(action);
        const displayNode = () =>  { this.animate(node, "opacity", 0.5, 2000); };
        this.thread.run(1, displayNode);
    }

    addConnectionWithAnimation(connectionData) {
        const line = this.addConnectionWithoutRender(connectionData);
        const displayLine = () =>  { this.animate(line, "opacity", 0.5, 2000); };
        this.thread.run(1, displayLine);
    }


     animate(toAnimate, attribute, value, duration) {
        toAnimate.animate(attribute, value, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: duration,
            easing: fabric.util.ease.easeInOutSine
        });
    }

}

const scanCanvas = new ScanCanvas();
export default scanCanvas