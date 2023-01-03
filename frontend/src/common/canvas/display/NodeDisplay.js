import {fabric} from "fabric";
import {toType} from "../../enums/NodeTypesNames";
import {CONNECTIONS, DISCOVERED, FREE, HACKED, PROTECTED, LAYERS, TYPE} from "../../enums/NodeStatus";
import {animate, easeInSine, easeOutSine} from "../CanvasUtils";
import {IMAGE_SIZE} from "./util/DisplayConstants";

const SCAN_OPACITY = 0.4;
const HACK_OPACITY = 1;

export default class NodeDisplay {

    id = null;
    nodeData = null;
    canvas = null;
    status = null;

    schedule = null;

    nodeIcon = null;
    labelIcon = null;
    labelBackgroundIcon = null;

    otherHackerDisplays = [];

    y = null;
    x = null;

    crossFadeResidualDelay = 0;

    constructor(canvas, schedule, nodeData, staticDisplay, hacking) {
        this.id = nodeData.id;
        this.canvas = canvas;
        this.schedule = schedule;
        this.nodeData = nodeData;
        this.hacking = hacking;
        this.x = nodeData.x;
        this.y = nodeData.y;

        this.nodeIcon = this.createNodeIcon(staticDisplay);
        this.canvas.add(this.nodeIcon);
        this.canvas.sendToBack(this.nodeIcon);

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

    createNodeIcon(staticDisplay) {
        const image = this.getNodeIconImage();
        const cursor = staticDisplay ? "pointer" : "move";

        const nodeIcon = new fabric.Image(image, {
            left: this.nodeData.x,
            top: this.nodeData.y,
            height: IMAGE_SIZE,
            width: IMAGE_SIZE,
            opacity: 0,
            data: this.nodeData,
            type: "node",
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: staticDisplay,
            lockMovementY: staticDisplay,
            hoverCursor: cursor,
        });
        nodeIcon.setControlsVisibility({
            tl: false,
            tr: false,
            br: false,
            bl: false,
            ml: false,
            mt: false,
            mr: false,
            mb: false,
            mtr: false,
        });

        return nodeIcon;
    }


    getNodeIconImage() {
        const imageStatus = this.determineStatusForIcon();
        const type = toType(this.nodeData.type);
        const imageId = type.name + "_" + imageStatus;
        return document.getElementById(imageId);
    }

    determineStatusForIcon() {
        if (this.nodeData.status === DISCOVERED || this.nodeData.status === TYPE || this.nodeData.status === CONNECTIONS) {
            return this.nodeData.status;
        }
        if (this.nodeData.ice) {
            if (this.nodeData.hacked) {
                return HACKED;
            } else {
                return PROTECTED;
            }
        } else {
            return FREE;
        }
    }

    appear() {
        this.schedule.run(3, () => {

            animate(this.canvas, this.labelIcon, "opacity", 1, 40);
            animate(this.canvas, this.labelBackgroundIcon, "opacity", 0.8, 40);
            animate(this.canvas, this.nodeIcon, "opacity", SCAN_OPACITY, 40);
        });
    }

    transitionToHack(quick) {
        const delay = (quick) ? 0 : 5;
        this.hacking = true;
        if (this.nodeData.status === LAYERS) {
            this.crossFadeToNewIcon(delay);
        }
    }

    crossFadeToNewIcon(delay) {
        const crossFadeTime = 20;
        this.crossFadeResidualDelay = crossFadeTime - delay;
        this.schedule.run(delay, () => {

            this.oldNodeIcon = this.nodeIcon;
            this.nodeIcon = this.createNodeIcon(true);
            this.canvas.add(this.nodeIcon);
            this.canvas.sendToBack(this.nodeIcon);
            this.canvas.sendToBack(this.oldNodeIcon);

            this.nodeIcon.set("left", this.x - 10);
            animate(this.canvas, this.oldNodeIcon, 'left', this.x - 10, crossFadeTime, easeOutSine);
            animate(this.canvas, this.nodeIcon, 'left', this.x, crossFadeTime, easeInSine);

            animate(this.canvas, this.nodeIcon, "opacity", this.determineNodeIconOpacity(), crossFadeTime, easeInSine);
            animate(this.canvas, this.oldNodeIcon, "opacity", 0, crossFadeTime, easeOutSine);
        });
    }


    cleanUpAfterCrossFade(canvasSelectedIcon) {
        this.schedule.wait(this.crossFadeResidualDelay);
        this.schedule.run(0, () => {
            if (!this.oldNodeIcon) {
                return;
            }
            this.canvas.remove(this.oldNodeIcon);
            if (canvasSelectedIcon === this.oldNodeIcon) {
                this.select();
            }
            this.oldNodeIcon = null;
        });
    }

    remove() {
        this.canvas.remove(this.nodeIcon);
        this.canvas.remove(this.labelIcon);
        this.canvas.remove(this.labelBackgroundIcon);
    }

    updateStatus(newStatus, canvasSelectedIcon) {
        this.nodeData.status = newStatus;
        this.updateNodeIcon(canvasSelectedIcon);
    }


    updateNodeIcon(canvasSelectedIcon) {
        this.crossFadeToNewIcon(5);
        this.cleanUpAfterCrossFade(canvasSelectedIcon);

    }


    determineNodeIconOpacity() {
        if (this.hacking) {
            switch (this.nodeData.status) {
                case LAYERS:
                    return HACK_OPACITY;
                default:
                    return SCAN_OPACITY;
            }
        } else {
            return SCAN_OPACITY;
        }
    }

    size() {
        return 33;
    }


    // Methods used by editor

    show() {
        this.nodeIcon.opacity = 1;
        this.labelIcon.opacity = 1;
        this.labelBackgroundIcon.opacity = 1;
        this.canvas.renderAll();
    }

    move(action) {
        this.nodeIcon.set({left: action.x, top: action.y});
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

    select() {
        this.canvas.setActiveObject(this.nodeIcon);
    }

    registerHacker(hackerDisplay) {
        this.otherHackerDisplays.push(hackerDisplay)
    }

    getYOffset(hackerDisplay) {
        const index = this.otherHackerDisplays.indexOf(hackerDisplay)
        return 10 + index * -20;
    }

    unregisterHacker(hackerDisplay) {
        const index = this.otherHackerDisplays.indexOf(hackerDisplay)
        if (index >= 0) {
            this.otherHackerDisplays.splice(index, 1);
        }
        this.otherHackerDisplays.forEach((otherHackerDisplay) => {
            const yOffset = this.getYOffset(otherHackerDisplay);
            otherHackerDisplay.repositionInNode(yOffset);
        });
    }

    hacked() {
        this.crossFadeToNewIcon(5);
    }

    terminate() {
        this.schedule.terminate();
    }

};