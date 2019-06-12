import {fabric} from "fabric";
import {toType} from "../enums/NodeTypesNames";
import {CONNECTIONS, DISCOVERED, FREE, PROTECTED, TYPE} from "../enums/NodeStatus";
import {animate} from "./CanvasUtils";

export default class NodeDisplay {

    nodeData = null;
    canvas = null;
    status = null;

    nodeIcon = null;
    labelIcon = null;
    labelBackgroundIcon = null;

    y = null;
    x = null;

    constructor(canvas, thread, nodeData, staticDisplay) {
        this.canvas = canvas;
        this.thread = thread;
        this.nodeData = nodeData;

        const image = this.getNodeIconImage();

        this.x = nodeData.x;
        this.y = nodeData.y;

        const cursor = staticDisplay ? "pointer" : "move";

        this.nodeIcon = new fabric.Image(image, {
            left: nodeData.x,
            top: nodeData.y,
            height: image.height,
            width: image.width,
            opacity: 0,
            data: nodeData,
            type: "node",
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: staticDisplay,
            lockMovementY: staticDisplay,
            hoverCursor: cursor,
        });
        this.nodeIcon.setControlsVisibility({
            ml: false,
            mt: false,
            mr: false,
            mb: false,
            mtr: false});

        this.canvas.add(this.nodeIcon);

        this.labelIcon = new fabric.Text(nodeData.networkId, {
            // fill: "#bbbbbb", // just simple grey
            fill: "#5cb85c",    // color-ok
            // fill: "#8cad8c", // color-ok muted (more grey)
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            // fontWeight: 10,
            left: nodeData.x - 20,
            top: nodeData.y + 35,
            textAlign: "left", // "center", "right" or "justify".
            opacity: 0,
            hoverCursor: 'default',
            selectable: false,
        });
        this.canvas.add(this.labelIcon);
        this.nodeIcon.label = this.labelIcon;

        this.labelBackgroundIcon = new fabric.Rect({
            width: 20,
            height: 20,
            fill: "#333333",
            left: nodeData.x - 20,
            top: nodeData.y + 35,
            opacity: 0,
            hoverCursor: 'default',
            selectable: false,
        });
        this.canvas.add(this.labelBackgroundIcon);
        this.nodeIcon.labelBackgroundIcon = this.labelBackgroundIcon;

        this.canvas.sendToBack(this.labelBackgroundIcon);
        this.canvas.bringToFront(this.labelIcon);
    }

    getNodeIconImage() {
        const imageStatus = this.determineStatusForIcon();
        const type = toType(this.nodeData.type);
        const imageId = type.name + "_" + imageStatus;
        return document.getElementById(imageId);
    }

    determineStatusForIcon() {
        if (this.nodeData.status === DISCOVERED || this.nodeData.status === TYPE) {
            return this.nodeData.status;
        }
        if (this.nodeData.status === CONNECTIONS) {
            return TYPE;
        }
        if (this.nodeData.ice) {
            return PROTECTED;
        }
        else {
            return FREE;
        }
    }

    appear() {
        this.thread.run(3, () => {

            animate(this.canvas, this.labelIcon, "opacity", 1, 40);
            animate(this.canvas, this.labelBackgroundIcon, "opacity", 0.8, 40);
            animate(this.canvas, this.nodeIcon, "opacity", 0.4, 40);
        });
    }

    remove() {
        this.canvas.remove(this.nodeIcon);
        this.canvas.remove(this.labelIcon);
        this.canvas.remove(this.labelBackgroundIcon);
    }

    updateStatus(newStatus) {
        this.nodeData.status = newStatus;
        if (newStatus !== CONNECTIONS) {
            this.updateNodeIcon(true);
        }

    }


    updateNodeIcon = (fadeOut) => {
        if (fadeOut) {
            this.thread.run(8, () => {
                animate(this.canvas, this.nodeIcon, 'opacity', 0.05, 8, fabric.util.ease.easeOutSine);
                animate(this.canvas, this.nodeIcon, 'left', "-=10", 8, fabric.util.ease.easeOutSine);
            });
        }

        const newIconImage = this.getNodeIconImage();

        this.thread.run(0, () => {
            this.nodeIcon.setElement(newIconImage);
            this.canvas.renderAll();
            const newOpacity = 0.4;
            // TODO check if we want opacity based on status...
            // const newOpacity = nodeOpacityByStatus[node.status];
            animate(this.canvas, this.nodeIcon, 'opacity', newOpacity, 8);
            if (fadeOut) {
                animate(this.canvas, this.nodeIcon, 'left', "+=10", 8, fabric.util.ease.easeInSine);
            }
        });
    };

    size() {
        return 22;
        // 22
        // 32
    }


    // Methods used by editor

    show() {
        this.nodeIcon.opacity = 1;
        this.labelIcon.opacity = 1;
        this.labelBackgroundIcon.opacity = 1;
        this.canvas.renderAll();
    }

    move(action) {
        this.nodeIcon.set({ left: action.x, top: action.y});
        this.nodeIcon.setCoords();
    }

    moving() {
        this.x = this.nodeIcon.left;
        this.y = this.nodeIcon.top;
        this.labelIcon.left = this.nodeIcon.left - 20;
        this.labelIcon.top = this.nodeIcon.top + 35;
        this.labelBackgroundIcon.left = this.nodeIcon.left - 20;
        this.labelBackgroundIcon.top = this.nodeIcon.top + 35;
    }

    updateNetworkId(networkId) {
        this.labelIcon.setText(networkId);
        this.canvas.renderAll();
    }

};