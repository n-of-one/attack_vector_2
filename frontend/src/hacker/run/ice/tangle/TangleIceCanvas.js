import {fabric} from "fabric";
import TanglePointDisplay from "./display/TanglePointDisplay";
import TangleLineDisplay from "./display/TangleLineDisplay";
import webSocketConnection from "../../../../common/WebSocketConnection";

class TangleIceCanvas {

    currentSelected = null;
    canvas = null;
    pointDisplayById = null;
    store = null;
    dispatch = null;


    init(puzzleData, dispatch, store) {

        this.dispatch = dispatch;
        this.store = store;

        this.canvas = new fabric.Canvas('untangleCanvas', {
            width: 1200,
            height: 680,
            backgroundColor: "#aaa",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        const canvas = this.canvas;

        setTimeout(function() {
            fabric.Image.fromURL("/img/frontier/ice/tangle/fractal-untangle-1200x680.png", (img) => {
                img.set({width: canvas.width, height: canvas.height, originX: 'left', originY: 'top'});
                canvas.setBackgroundImage(img, canvas.renderAll.bind(canvas));
            });
        }, 100);

        // this.canvas.on('object:modified', (event) => { this.canvasObjectModified(event); });
        this.canvas.on('selection:created', (event) => {
            this.canvasSelectionCreated(event);
        });
        this.canvas.on('selection:cleared', (event) => {
            this.canvasSelectionCleared(event);
        });
        this.canvas.on('mouse:up', (event) => {
            this.canvasSelectionCleared(event);
        });
        this.canvas.on('object:moving', (event) => {
            this.canvasObjectMoved(event);
        });
        this.canvas.selection = false;

        const {points, lines} = puzzleData;

        this.pointDisplayById = {};

        points.forEach(point => this.addPoint(point));
        lines.forEach(lines => this.addLine(lines));
        Object.values(this.pointDisplayById).forEach(pointDisplay => pointDisplay.show());


        this.canvas.discardActiveObject();
        this.canvas.renderAll();
    }

    addPoint(pointData) {
        const pointDisplay = new TanglePointDisplay(this.canvas, pointData);
        this.pointDisplayById[pointData.id] = pointDisplay;
    }

    addLine(lineData) {
        const fromDisplay = this.pointDisplayById[lineData.fromId];
        const toDisplay = this.pointDisplayById[lineData.toId];

        const lineDisplay = new TangleLineDisplay(this.canvas, lineData.id, fromDisplay, toDisplay, lineData.type);
        fromDisplay.addLine(lineDisplay);
        toDisplay.addLine(lineDisplay);

        lineDisplay.show();
    }

    canvasSelectionCreated(event) {
        const selectedObject = event.selected[0];
        if (selectedObject.display) {
            this.currentSelected = selectedObject.display;
            this.currentSelected.highLight();
            this.canvas.renderAll();
        }
    }

    canvasSelectionCleared(event) {
        if (this.currentSelected) {
            const icon = this.currentSelected.icon;
            // this.dispatch({type: ICE_TANGLE_MOVE_POINT, id: this.currentSelected.id, x: icon.left, y: icon.top});

            const state = this.store.getState();

            const runId = state.run.scan.runId;
            const currentIce = state.run.ice.currentIce;

            const payload = {layerId: currentIce.layerId, runId: runId, id: this.currentSelected.id, x: icon.left, y: icon.top};
            webSocketConnection.send("/av/ice/tangle/moved", JSON.stringify(payload));


            this.currentSelected.unHighlight();
            this.currentSelected = null;
            this.canvas.discardActiveObject().renderAll();
        }
    }

    canvasObjectMoved(event) {
        const id = event.target.display.id;
        const pointDisplay = this.pointDisplayById[id];
        pointDisplay.updateLines();
        this.canvas.renderAll();
    }

    serverMovedPoint(actionData) {
        this.pointDisplayById[actionData.id].moved(actionData.x, actionData.y);
        this.canvas.renderAll();
    }
}


const tangleIceCanvas = new TangleIceCanvas();
export default tangleIceCanvas
