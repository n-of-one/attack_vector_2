import {fabric} from "fabric"
import {toType} from "../../enums/NodeTypesNames"
import {CONNECTIONS_KNOWN_3, DISCOVERED_1, FREE, HACKED, PROTECTED, FULLY_SCANNED_4, TYPE_KNOWN_2, NodeScanStatus} from "../../enums/NodeStatus"
import {getHtmlImage} from "../CanvasUtils"
import {IMAGE_SIZE} from "./util/DisplayConstants"
import {Display, Graphics} from "./Display"
import {Schedule} from "../../util/Schedule"
import {MoveNodeI, NodeI} from "../../../editor/reducer/NodesReducer"
import {HackerDisplay} from "./HackerDisplay"
import {delayTicks} from "../../util/Util";

const SCAN_OPACITY = 0.3
const HACK_OPACITY = 1

export enum SiteStatus {
    SCANNING, HACKING, SHUTDOWN
}

export class NodeDisplay implements Display {

    canvas: fabric.Canvas
    schedule: Schedule | null
    gfx: Graphics

    id: string
    nodeData: NodeI

    siteStatus: SiteStatus
    movable: boolean

    nodeIcon: fabric.Image = null as unknown as fabric.Image // constructor init via this.addNodeIcon()
    labelIcon: fabric.Text
    labelBackgroundIcon: fabric.Rect
    crossFadeNodeIcon: fabric.Image | null = null

    otherHackerDisplays: HackerDisplay[] = []

    y: number
    x: number
    size: number = 33

    constructor(canvas: fabric.Canvas, schedule: Schedule | null, nodeData: NodeI, movable: boolean, iconStatus: SiteStatus) {
        this.id = nodeData.id
        this.canvas = canvas
        this.schedule = schedule
        this.gfx = new Graphics(canvas)
        this.nodeData = nodeData
        this.siteStatus = iconStatus
        this.x = nodeData.x
        this.y = nodeData.y
        this.movable = movable

        this.addNodeIcon()

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
        })
        this.canvas.add(this.labelIcon)

        this.labelBackgroundIcon = new fabric.Rect({
            width: 20,
            height: 20,
            fill: "#333333",
            left: nodeData.x - 20,
            top: nodeData.y + 35,
            opacity: 0,
            hoverCursor: 'default',
            selectable: false,
        })
        this.canvas.add(this.labelBackgroundIcon)

        this.canvas.sendToBack(this.labelBackgroundIcon)
        this.canvas.bringToFront(this.labelIcon)
    }

    getAllIcons() {
        if (this.crossFadeNodeIcon != null) {
            return [this.nodeIcon, this.labelIcon, this.labelBackgroundIcon, this.crossFadeNodeIcon]
        }
        return [this.nodeIcon, this.labelIcon, this.labelBackgroundIcon]
    }

    addNodeIcon() {
        this.nodeIcon = this.createNodeIcon()
        this.canvas.add(this.nodeIcon)
        this.canvas.sendToBack(this.nodeIcon)
    }

    private createNodeIcon() {
        if (this.siteStatus === SiteStatus.SHUTDOWN) {
            return this.createShutdownNodeIcon()
        }

        const image = this.getNodeIconImage()
        return this.createNodeIconInternal(image)
    }

    createShutdownNodeIcon() {
        const type = toType(this.nodeData.type)
        const imageId = type.name + "_DISCOVERED"
        const image = getHtmlImage(imageId)

        return this.createNodeIconInternal(image)
    }

    private createNodeIconInternal(image: HTMLImageElement) {
        const cursor = this.movable ? "move" : "pointer"

        const nodeIcon = new fabric.Image(image, {
            left: this.nodeData.x, top: this.nodeData.y,
            height: IMAGE_SIZE, width: IMAGE_SIZE,
            opacity: 0,
            data: this.nodeData,
            type: "node",
            lockRotation: true,
            lockScalingX: true, lockScalingY: true,
            lockMovementX: !this.movable, lockMovementY: !this.movable,
            hoverCursor: cursor,
        })
        nodeIcon.setControlsVisibility({tl: false, tr: false, br: false, bl: false, ml: false, mt: false, mr: false, mb: false, mtr: false,})

        return nodeIcon
    }


    getNodeIconImage(): HTMLImageElement {
        const nodeStatus = this.determineNodeStatusForIcon()
        const type = toType(this.nodeData.type)

        const imageId = type.name + "_" + nodeStatus
        return getHtmlImage(imageId)
    }

    determineNodeStatusForIcon() {
        switch (this.nodeData.status) {
            case DISCOVERED_1:
                return "DISCOVERED"
            case TYPE_KNOWN_2:
                return "TYPE"
            case CONNECTIONS_KNOWN_3:
                return "CONNECTIONS"
            case FULLY_SCANNED_4:
                if (this.nodeData.ice) {
                    if (this.nodeData.hacked) {
                        return HACKED
                    } else {
                        return PROTECTED
                    }
                } else {
                    return FREE
                }
            default:
                throw Error("Unknown status for node: " + this.nodeData.status)
        }
    }

    appear() {
        if (!this.schedule) throw new Error("schedule not initialized")

        this.schedule.run(3, () => {

            this.gfx.fade(40, 1, this.labelIcon)
            this.gfx.fade(40, 0.8, this.labelBackgroundIcon)
            this.gfx.fade(40, SCAN_OPACITY, this.nodeIcon)

            // animate(this.canvas, this.labelIcon, "opacity", 1, 40)
            // animate(this.canvas, this.labelBackgroundIcon, "opacity", 0.8, 40)
            // animate(this.canvas, this.nodeIcon, "opacity", SCAN_OPACITY, 40)
        })
    }

    transitionToHack(quick: boolean, canvasSelectedIcon: fabric.Image | null) {
        const delay = (quick) ? 0 : 5
        this.siteStatus = SiteStatus.HACKING
        if (this.nodeData.status === FULLY_SCANNED_4) {
            this.crossFadeToNewIconStart(delay, canvasSelectedIcon)
        }
    }

    transitionToScan() {
        this.siteStatus = SiteStatus.SCANNING
        this.gfx.fade(20, this.determineNodeIconOpacity(), this.nodeIcon)
    }


    crossFadeToNewIconStart(delay: number, canvasSelectedIcon: fabric.Image | null) {
        if (this.schedule == null) throw new Error("schedule not initialized")

        const crossFadeTime = 20
        this.schedule.run(delay, () => {

            this.crossFadeNodeIcon = this.nodeIcon
            this.addNodeIcon()
            this.canvas.sendToBack(this.crossFadeNodeIcon)

            // this.nodeIcon.set("left", this.x - 10)
            // animate(this.canvas, this.oldNodeIcon, 'left', this.x - 10, crossFadeTime)
            // animate(this.canvas, this.nodeIcon, 'left', this.x, crossFadeTime)

            this.gfx.fade(crossFadeTime, this.determineNodeIconOpacity(), this.nodeIcon)
            this.gfx.fadeOut(crossFadeTime, this.crossFadeNodeIcon)

            // animate(this.canvas, this.nodeIcon, "opacity", this.determineNodeIconOpacity(), crossFadeTime)
            // animate(this.canvas, this.oldNodeIcon, "opacity", 0, crossFadeTime)

            delayTicks(crossFadeTime, () => {
                if (!this.crossFadeNodeIcon) {
                    return
                }
                this.canvas.remove(this.crossFadeNodeIcon)
                if (canvasSelectedIcon === this.crossFadeNodeIcon) {
                    this.select()
                }
                this.crossFadeNodeIcon = null
            })
        })

    }

    remove() {
        this.canvas.remove(this.nodeIcon)
        this.canvas.remove(this.labelIcon)
        this.canvas.remove(this.labelBackgroundIcon)
    }

    updateStatus(newStatus: NodeScanStatus, canvasSelectedIcon: fabric.Image | null) {
        this.nodeData.status = newStatus
        this.updateNodeIcon(canvasSelectedIcon)
    }


    updateNodeIcon(canvasSelectedIcon: fabric.Image | null) {
        this.crossFadeToNewIconStart(5, canvasSelectedIcon)
    }


    determineNodeIconOpacity() {
        if (this.siteStatus === SiteStatus.HACKING) {
            switch (this.nodeData.status) {
                case FULLY_SCANNED_4:
                    return HACK_OPACITY
                default:
                    return SCAN_OPACITY
            }
        } else {
            return SCAN_OPACITY
        }
    }

    // Methods used by editor

    show() {
        this.nodeIcon.opacity = 1
        this.labelIcon.opacity = 1
        this.labelBackgroundIcon.opacity = 1
        this.canvas.renderAll()
    }

    move(action: MoveNodeI) {
        this.nodeIcon.set({left: action.x, top: action.y})
        this.nodeIcon.setCoords()
    }

    moving() {
        this.x = this.nodeIcon.left!
        this.y = this.nodeIcon.top!
        this.labelIcon.left = this.nodeIcon.left! - 20
        this.labelIcon.top = this.nodeIcon.top! + 35
        this.labelBackgroundIcon.left = this.nodeIcon.left! - 20
        this.labelBackgroundIcon.top = this.nodeIcon.top! + 35
    }

    updateNetworkId(networkId: string) {
        this.labelIcon.text = networkId
        this.canvas.renderAll()
    }

    select() {
        this.canvas.setActiveObject(this.nodeIcon).requestRenderAll()
    }

    registerHacker(hackerDisplay: HackerDisplay) {
        this.otherHackerDisplays.push(hackerDisplay)
    }

    getYOffset(hackerDisplay: HackerDisplay) {
        const index = this.otherHackerDisplays.indexOf(hackerDisplay)
        return 10 + index * -20
    }

    unregisterHacker(hackerDisplay: HackerDisplay) {
        const index = this.otherHackerDisplays.indexOf(hackerDisplay)
        if (index >= 0) {
            this.otherHackerDisplays.splice(index, 1)
        }
        this.otherHackerDisplays.forEach((otherHackerDisplay) => {
            const yOffset = this.getYOffset(otherHackerDisplay)
            otherHackerDisplay.repositionInNode(yOffset)
        })
    }

    hacked(canvasSelectedIcon: fabric.Image | null) {
        this.crossFadeToNewIconStart(5, canvasSelectedIcon)
    }

    terminate() {
        if (this.schedule != null) this.schedule.terminate()
    }
    siteShutdownStart() {
        if (this.schedule == null) throw new Error("schedule not initialized")

        const crossFadeTime = 20
        this.schedule.run(3, () => {
            this.crossFadeNodeIcon = this.createShutdownNodeIcon()
            this.canvas.add(this.crossFadeNodeIcon)
            this.canvas.sendToBack(this.crossFadeNodeIcon)
            this.canvas.sendToBack(this.nodeIcon)

            this.gfx.fade(crossFadeTime, 1, this.crossFadeNodeIcon)
            this.gfx.fadeOut(crossFadeTime, this.nodeIcon)
        })
    }

    siteShutdownFinish() {
        if (this.schedule == null) throw new Error("schedule not initialized")

        const crossFadeTime = 20
        this.schedule.run(3, () => {
            this.siteStatus = SiteStatus.SCANNING
            this.canvas.remove(this.nodeIcon)
            this.addNodeIcon()

            this.gfx.fadeOut(crossFadeTime, this.crossFadeNodeIcon!)
            this.gfx.fade(crossFadeTime, this.determineNodeIconOpacity(), this.nodeIcon)

            delayTicks(crossFadeTime, () => {
                this.canvas.remove(this.crossFadeNodeIcon!)
                this.crossFadeNodeIcon = null
            })
        })
    }
}