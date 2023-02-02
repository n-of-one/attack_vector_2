import {fabric} from "fabric";
import {toType} from "../../enums/NodeTypesNames";
import {CONNECTIONS, DISCOVERED, FREE, HACKED, PROTECTED, LAYERS, TYPE} from "../../enums/NodeStatus";
import {animate, getHtmlImage} from "../CanvasUtils";
import {IMAGE_SIZE} from "./util/DisplayConstants";
import {Display} from "./Display";
import {Schedule} from "../../Schedule";
import {MoveNodeI, NodeI} from "../../../editor/reducer/NodesReducer";
import {NodeStatus} from "../../../hacker/run/reducer/ScanReducer";
import {HackerDisplay} from "./HackerDisplay";

const SCAN_OPACITY = 0.4;
const HACK_OPACITY = 1;

export class NodeDisplay implements Display {

    id: string
    nodeData: NodeI
    canvas: fabric.Canvas
    schedule: Schedule | null

    status = null
    hacking: boolean

    nodeIcon: fabric.Image
    labelIcon: fabric.Text
    labelBackgroundIcon: fabric.Rect
    oldNodeIcon: fabric.Image | null = null

    otherHackerDisplays: HackerDisplay[] = []

    y: number
    x: number
    size: number = 33

    crossFadeResidualDelay = 0;

    constructor(canvas: fabric.Canvas, schedule: Schedule | null, nodeData: NodeI, movable: boolean, hacking: boolean) {
        this.id = nodeData.id;
        this.canvas = canvas;
        this.schedule = schedule;
        this.nodeData = nodeData;
        this.hacking = hacking;
        this.x = nodeData.x;
        this.y = nodeData.y;

        this.nodeIcon = this.createNodeIcon(movable);
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

        // TODO We don't need this, right?
        // remove:
        // this.nodeIcon.label = this.labelIcon;

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

        // TODO We don't need this, right?
        // remove:
        // this.nodeIcon.labelBackgroundIcon = this.labelBackgroundIcon;

        this.canvas.sendToBack(this.labelBackgroundIcon);
        this.canvas.bringToFront(this.labelIcon);
    }

    getAllIcons() {
        if (this.oldNodeIcon != null) {
            return [this.nodeIcon, this.labelIcon, this.labelBackgroundIcon, this.oldNodeIcon]
        }
        return [this.nodeIcon, this.labelIcon, this.labelBackgroundIcon]
    }

    createNodeIcon(movable: boolean) {
        const image = this.getNodeIconImage();
        const cursor = movable ? "pointer" : "move";

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
            lockMovementX: movable,
            lockMovementY: movable,
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


    getNodeIconImage(): HTMLImageElement {
        const imageStatus = this.determineStatusForIcon();
        const type = toType(this.nodeData.type);

        const imageId = type.name + "_" + imageStatus;
        return getHtmlImage(imageId)
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
        if (!this.schedule) throw new Error("schedule not initialized")

        this.schedule.run(3, () => {

            animate(this.canvas, this.labelIcon, "opacity", 1, 40);
            animate(this.canvas, this.labelBackgroundIcon, "opacity", 0.8, 40);
            animate(this.canvas, this.nodeIcon, "opacity", SCAN_OPACITY, 40);
        });
    }

    transitionToHack(quick: boolean) {
        const delay = (quick) ? 0 : 5;
        this.hacking = true;
        if (this.nodeData.status === LAYERS) {
            this.crossFadeToNewIcon(delay);
        }
    }

    crossFadeToNewIcon(delay: number) {
        if (this.schedule == null) throw new Error("schedule not initialized")

        const crossFadeTime = 20;
        this.crossFadeResidualDelay = crossFadeTime - delay;
        this.schedule.run(delay, () => {

            this.oldNodeIcon = this.nodeIcon;
            this.nodeIcon = this.createNodeIcon(true);
            this.canvas.add(this.nodeIcon);
            this.canvas.sendToBack(this.nodeIcon);
            this.canvas.sendToBack(this.oldNodeIcon);

            // this.nodeIcon.set("left", this.x - 10);
            // animate(this.canvas, this.oldNodeIcon, 'left', this.x - 10, crossFadeTime);
            // animate(this.canvas, this.nodeIcon, 'left', this.x, crossFadeTime);

            animate(this.canvas, this.nodeIcon, "opacity", this.determineNodeIconOpacity(), crossFadeTime);
            animate(this.canvas, this.oldNodeIcon, "opacity", 0, crossFadeTime);
        });
    }


    cleanUpAfterCrossFade(canvasSelectedIcon: fabric.Image | null) {
        if (this.schedule == null) throw new Error("schedule not initialized")

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

    updateStatus(newStatus: NodeStatus, canvasSelectedIcon: fabric.Image | null) {
        this.nodeData.status = newStatus;
        this.updateNodeIcon(canvasSelectedIcon);
    }


    updateNodeIcon(canvasSelectedIcon: fabric.Image | null) {
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

    // Methods used by editor

    show() {
        this.nodeIcon.opacity = 1;
        this.labelIcon.opacity = 1;
        this.labelBackgroundIcon.opacity = 1;
        this.canvas.renderAll();
    }

    move(action: MoveNodeI) {
        this.nodeIcon.set({left: action.x, top: action.y});
        this.nodeIcon.setCoords();
    }

    moving() {
        this.x = this.nodeIcon.left!;
        this.y = this.nodeIcon.top!;
        this.labelIcon.left = this.nodeIcon.left! - 20;
        this.labelIcon.top = this.nodeIcon.top! + 35;
        this.labelBackgroundIcon.left = this.nodeIcon.left! - 20;
        this.labelBackgroundIcon.top = this.nodeIcon.top! + 35;
    }

    updateNetworkId(networkId: string) {
        this.labelIcon.text = networkId;
        this.canvas.renderAll();
    }

    select() {
        this.canvas.setActiveObject(this.nodeIcon).requestRenderAll();
    }

    registerHacker(hackerDisplay: HackerDisplay) {
        this.otherHackerDisplays.push(hackerDisplay)
    }

    getYOffset(hackerDisplay: HackerDisplay) {
        const index = this.otherHackerDisplays.indexOf(hackerDisplay)
        return 10 + index * -20;
    }

    unregisterHacker(hackerDisplay: HackerDisplay) {
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
        if (this.schedule != null) this.schedule.terminate();
    }

};