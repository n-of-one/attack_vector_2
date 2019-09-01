import {fabric} from "fabric";
import TanglePointDisplay from "./TanglePointDisplay";
import TangleLine from "./TangleLine";

class TangleCanvas {

    currentSelected = null;
    canvas = null;
    pointDisplayById = null;


    fakeInput = {
        points: [
            {id: 1, x: 250, y: 80},
            {id: 2, x: 450, y: 80},
            {id: 3, x: 150, y: 280},
            {id: 4, x: 650, y: 280},
            {id: 5, x: 250, y: 480},
            {id: 6, x: 450, y: 480},
        ],
        lines: [
            {id: 1, fromId: 1, toId: 2},
            {id: 2, fromId: 1, toId: 3},
            {id: 3, fromId: 1, toId: 5},
            {id: 5, fromId: 2, toId: 3},
            {id: 4, fromId: 2, toId: 5},
            {id: 8, fromId: 3, toId: 6},
            {id: 9, fromId: 4, toId: 5},
            {id: 10, fromId: 4, toId: 6},
        ]
    };

    init() {
        this.canvas = new fabric.Canvas('untangleCanvas', {
            width: 800,
            height: 800,
            backgroundColor: "#aaa",
        });

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        // this.canvas.on('object:modified', (event) => { this.canvasObjectModified(event); });
        this.canvas.on('object:selected', (event) => {
            this.canvasObjectSelected(event);
        });
        this.canvas.on('selection:cleared', (event) => {
            this.canvasObjectDeSelected(event);
        });
        this.canvas.on('mouse:up', (event) => {
            this.canvasObjectDeSelected(event);
        });
        this.canvas.on('object:moving', (event) => {
            this.canvasObjectMoved(event);
        });
        this.canvas.selection = false;

        const {points, lines} = this.fakeInput;

        this.pointDisplayById = {};

        points.forEach(point => this.addPoint(point));
        lines.forEach(lines => this.addLine(lines));
        Object.values(this.pointDisplayById).forEach(pointDisplay => pointDisplay.show());


        this.canvas.deactivateAll();
        this.canvas.renderAll();
    }

    addPoint(pointData) {
        const pointDisplay = new TanglePointDisplay(this.canvas, pointData);
        this.pointDisplayById[pointData.id] = pointDisplay;
    }

    addLine(lineData) {
        const fromDisplay = this.pointDisplayById[lineData.fromId];
        const toDisplay = this.pointDisplayById[lineData.toId];

        const lineDisplay = new TangleLine(this.canvas, lineData.id, fromDisplay, toDisplay);
        fromDisplay.addLine(lineDisplay);
        toDisplay.addLine(lineDisplay);

        lineDisplay.show();
    }

    canvasObjectSelected(event) {
        const image = event.target;
        if (image.display) {
            this.currentSelected = image.display;
            this.currentSelected.highLight();
            this.canvas.renderAll();
        }
    }

    canvasObjectDeSelected(event) {
        if (this.currentSelected) {
            this.currentSelected.unHighlight();
            this.currentSelected = null;
            this.canvas.deactivateAll().renderAll();
        }
    }

    canvasObjectMoved(event) {
        const id = event.target.display.id;
        const pointDisplay = this.pointDisplayById[id];
        pointDisplay.updateLines();
        this.canvas.renderAll();
    }

}


const untangleCanvas = new TangleCanvas();
export default untangleCanvas
