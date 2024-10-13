import {fabric} from "fabric"
import {animate, calcLine, easeInOutSine, easeOutSine, getHtmlImage, LinePositions} from "../CanvasUtils"
import {Schedule} from "../../util/Schedule"
import {
    COLOR_HACKER_LINE,
    IDENTIFICATION_SCALE_LARGE,
    IDENTIFICATION_SCALE_NORMAL,
    IMAGE_SIZE,
    OFFSET,
    SCALE_NORMAL,
    SCALE_SMALL
} from "./util/DisplayConstants"
import {ConnectionVisual} from "../visuals/ConnectionVisual"
import {Display, Graphics} from "./Display"
import {Dispatch} from "redux"
import {DisplayCollection} from "./util/DisplayCollection"
import {NodeDisplay} from "./NodeDisplay"
import {Canvas, IUtilAminEaseFunction} from "fabric/fabric-impl"
import {HackerActivity, HackerPresence} from "../../../hacker/run/reducer/HackersReducer"
import {notEmpty} from "../../util/Util";
import {Timings} from "../../model/Ticks";
import {CANVAS_HEIGHT} from "../CanvasConst";

const APPEAR_TIME = 20
const DISAPPEAR_TIME = 10


const IDENTIFIER_OPACITY_SCANNING = 0.1
const IDENTIFIER_OPACITY_HACKING = 0.5

const LINE_OPACITY_SCANNING = 0.5
const LINE_OPACITY_HACKING = 1


export class HackerDisplay implements Display {

    canvas: fabric.Canvas
    schedule: Schedule
    gfx: Graphics

    dispatch: Dispatch
    nodeDisplays: DisplayCollection<NodeDisplay>

    hackerData: HackerPresence
    you: boolean
    siteStartNodeDisplay: NodeDisplay
    currentNodeDisplay: NodeDisplay | null = null

    /// The icon of the hacker inside the site (only when hacking, not when scanning)
    hackerIcon: fabric.Image | null = null

    /// The icon at the bottom of the screen, and the hider to cut off the bottom part

    // all are created in method called from constructor
    hackerIdentifierIcon: fabric.Image = null as unknown as fabric.Image
    hackerHider: fabric.Rect = null as unknown as fabric.Rect
    labelIcon: fabric.Text = null as unknown as fabric.Text
    startLineIcon: fabric.Line = null as unknown as fabric.Line

    moveLineElement: ConnectionVisual | null = null

    targetNodeDisplay: NodeDisplay | null = null

    y: number
    x: number

    size = 30

    constructor(
        canvas: Canvas,
        siteStartNodeDisplay: NodeDisplay,
        hackerData: HackerPresence,
        offset: number,
        you: boolean,
        dispatch: Dispatch,
        nodeDisplayById: DisplayCollection<NodeDisplay>) {


        this.canvas = canvas
        this.gfx = new Graphics(canvas)
        this.schedule = new Schedule(dispatch)
        this.hackerData = hackerData

        this.you = you
        this.siteStartNodeDisplay = siteStartNodeDisplay

        this.dispatch = dispatch
        this.nodeDisplays = nodeDisplayById

        this.x = offset
        this.y = CANVAS_HEIGHT - 25

        this.addHackerIdentificationIcons()

        let identifierOpacity, lineOpacity

        if (hackerData.activity === HackerActivity.OUTSIDE) {
            this.currentNodeDisplay = null

            identifierOpacity = IDENTIFIER_OPACITY_SCANNING
            lineOpacity = LINE_OPACITY_SCANNING

        } else {
            this.addHackingHacker(hackerData)

            identifierOpacity = IDENTIFIER_OPACITY_HACKING
            lineOpacity = LINE_OPACITY_HACKING
        }

        this.gfx.fade(APPEAR_TIME, identifierOpacity, this.hackerIdentifierIcon)
        this.gfx.fade(APPEAR_TIME, lineOpacity, this.labelIcon)
        this.gfx.fade(40, lineOpacity, this.startLineIcon)

        this.schedule.wait(3)
    }

    getAllIcons(): fabric.Object[] {
        const potentials: (fabric.Object | null)[] = [this.hackerIcon, this.hackerIdentifierIcon, this.hackerHider, this.labelIcon, this.startLineIcon]
        if (this.moveLineElement) {
            potentials.push(this.moveLineElement.line)
        }
        return potentials.filter(notEmpty)
    }

    addHackingHacker(hackerData: HackerPresence) {
        // If the hacker is in transit or still starting the run, then we are displaying them as arrived at the node.

        if (hackerData.nodeId === null || hackerData.nodeId === undefined) throw Error("nodeId undefined: " + JSON.stringify(hackerData))

        this.currentNodeDisplay = this.nodeDisplays.get(hackerData.nodeId)

        this.currentNodeDisplay.registerHacker(this)
        const {xOffset, yOffset} = this.calculateOffset(this.currentNodeDisplay)
        this.hackerIcon = this.createHackerIcon(SCALE_NORMAL, 1, this.currentNodeDisplay, xOffset, yOffset)
        this.canvas.add(this.hackerIcon)
    }

    createHackerIcon(scale: number, opacity: number, display: Display, offsetX: number = 0, offsetY: number = 0) {
        const id = this.you ? `${this.hackerData.icon}-red` : this.hackerData.icon
        const image = getHtmlImage(id)
        const icon = new fabric.Image(image, {
            left: display.x + offsetX,
            top: display.y + offsetY,
            height: IMAGE_SIZE,
            width: IMAGE_SIZE,
            scaleX: scale,
            scaleY: scale,
            opacity: opacity,
            selectable: false,
            hoverCursor: "default",
        })
        return icon
    }

    addHackerIdentificationIcons() {
        const size = this.you ? IDENTIFICATION_SCALE_LARGE : IDENTIFICATION_SCALE_NORMAL
        this.hackerIdentifierIcon = this.createHackerIcon(size, 0, this)
        this.hackerHider = new fabric.Rect({
            left: this.x,
            top: this.y + 25,
            height: 35,
            width: 40,
            opacity: 1,
            fill: "#333333",
            selectable: false,
            hoverCursor: "default",
        })
        const hackerName = this.you ? "You" : this.hackerData.userName
        this.labelIcon = new fabric.Text(hackerName, {
            fill: "#f0ad4e",    // color-ok
            fontFamily: "SpaceMono",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            left: this.x,
            top: this.y + 15,
            textAlign: "center", // "center", "right" or "justify".
            opacity: 0,
            selectable: false,
            hoverCursor: "default",
        })
        const lineData = calcLine(this, this.siteStartNodeDisplay)
        this.startLineIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#bb8",
                strokeWidth: 1,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            })

        this.canvas.add(this.hackerIdentifierIcon)
        this.canvas.add(this.hackerHider)
        this.canvas.add(this.labelIcon)
        this.canvas.add(this.startLineIcon)
        this.canvas.sendToBack(this.startLineIcon)

    }

    repositionHackerIdentification(newX: number) {
        this.schedule.run(APPEAR_TIME, () => {
            this.x = newX
            animate(this.canvas, this.hackerIdentifierIcon, "left", newX, APPEAR_TIME)
            animate(this.canvas, this.hackerHider, "left", newX, APPEAR_TIME)
            animate(this.canvas, this.labelIcon, "left", newX, APPEAR_TIME)
            animate(this.canvas, this.hackerHider, "left", newX, APPEAR_TIME)
            const lineData = calcLine(this, this.siteStartNodeDisplay)
            animate(this.canvas, this.startLineIcon, null, lineData.asCoordinates(), APPEAR_TIME)
        })
    }

    disappear() {
        this.schedule.run(DISAPPEAR_TIME, () => {
            if (this.currentNodeDisplay) {
                this.currentNodeDisplay.unregisterHacker(this)
            }
            this.gfx.fadeOut(DISAPPEAR_TIME, this.hackerIdentifierIcon )
            this.gfx.fadeOut(DISAPPEAR_TIME, this.startLineIcon)
            this.gfx.fadeOut(DISAPPEAR_TIME, this.labelIcon)

            // animate(this.canvas, this.hackerIdentifierIcon, "opacity", 0, DISAPPEAR_TIME)
            // animate(this.canvas, this.startLineIcon, "opacity", 0, DISAPPEAR_TIME)
            // animate(this.canvas, this.labelIcon, "opacity", 0, DISAPPEAR_TIME)
            if (this.hackerIcon) {
                this.gfx.fadeOut(DISAPPEAR_TIME, this.hackerIcon)
                // animate(this.canvas, this.hackerIcon, "opacity", 0, DISAPPEAR_TIME)
            }
        })
        this.schedule.run(0, () => {
            this.canvas.remove(this.hackerIdentifierIcon)
            this.canvas.remove(this.startLineIcon)
            this.canvas.remove(this.labelIcon)
            this.canvas.remove(this.hackerHider)
            if (this.hackerIcon) {
                this.canvas.remove(this.hackerIcon)
            }
        })
    }

    startRun(quick: boolean, timings: Timings) {
        if (quick) {
            this.startRunQuick()
        } else {
            this.startRunSlow(timings)
        }
    }

    startRunQuick() {
        this.hackerIcon = this.createHackerIcon(SCALE_NORMAL, 1, this.siteStartNodeDisplay)
        this.canvas.add(this.hackerIcon)
        this.canvas.bringToFront(this.hackerIcon)
        animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 10)
        animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, 10)
        animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, 10)

        animate(this.canvas, this.hackerIcon!, 'opacity', 1, 5)

    }

    startRunSlow(timings: Timings) {
        this.hackerIcon = this.createHackerIcon(SCALE_NORMAL, 0, this)
        this.canvas.add(this.hackerIcon)
        this.canvas.sendToBack(this.hackerIcon)
        this.hackerIcon.opacity = 0
        this.targetNodeDisplay = this.siteStartNodeDisplay

        this.schedule.run(timings.main, () => {
            if (!this.targetNodeDisplay) throw Error("!this.targetNodeDisplay")
            this.moveLineElement = this.animateMoveStepLine(this, this.targetNodeDisplay, timings.main - 50, this.you, easeOutSine)
        })
        animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 200)
        animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, 100)
        animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, 100)
    }

    moveStart(nodeDisplay: NodeDisplay, timings: Timings) {
        this.targetNodeDisplay = nodeDisplay

        this.schedule.run(timings.main, () => {
            if (!this.currentNodeDisplay) throw Error("!this.currentNodeDisplay")
            this.moveLineElement = this.animateMoveStepLine(this.currentNodeDisplay, nodeDisplay, timings.main + 10, this.you)
        })

    }

    moveArrive(nodeDisplay: NodeDisplay, timings: Timings) {
        this.schedule.run(0, () => {
            this.moveLineElement?.disappear(10)
        })

        const oldNodeDisplay = this.currentNodeDisplay
        this.currentNodeDisplay = nodeDisplay
        this.currentNodeDisplay.registerHacker(this)

        this.targetNodeDisplay = null

        const newOffset = this.calculateOffset(nodeDisplay)

        this.hackerIcon!.left = nodeDisplay.x + newOffset.xOffset
        this.hackerIcon!.top = nodeDisplay.y + newOffset.yOffset
        this.hackerIcon!.opacity = 0
        this.canvas.bringToFront(this.hackerIcon!)
        animate(this.canvas, this.hackerIcon!, 'opacity', 1, timings.main)

        if (oldNodeDisplay == null) {
            // This is the first move from outside
            return
        }
        const oldOffset = this.calculateOffset(oldNodeDisplay)
        oldNodeDisplay.unregisterHacker(this)
        const afterImage = this.createHackerIcon(SCALE_NORMAL, 1, oldNodeDisplay, oldOffset.xOffset, oldOffset.yOffset)
        this.canvas.add(afterImage).renderAll()
        animate(this.canvas, afterImage, 'opacity', 0, timings.main)
        this.schedule.wait(timings.main)
        this.schedule.run(0, () => {
            this.canvas.remove(afterImage)
        })

    }

    calculateOffset(nodeDisplay: NodeDisplay): { xOffset: number, yOffset: number } {
        if (this.you) {
            return {xOffset: OFFSET, yOffset: OFFSET}
        } else {
            const yOffset = nodeDisplay.getYOffset(this)
            return {xOffset: -OFFSET, yOffset: yOffset}
        }
    }

    repositionInNode(yOffset: number) {
        this.schedule.run(4, () => {
            if (!this.currentNodeDisplay) throw Error("!this.currentNodeDisplay")

            this.moveStep(this.currentNodeDisplay, -OFFSET, yOffset, 4)
        })
    }

    moveStep(node: NodeDisplay, offsetX: number, offsetY: number, time: number, easing: IUtilAminEaseFunction | null = null) {
        animate(this.canvas, this.hackerIcon!, "left", node.x + offsetX, time, easing)
        animate(this.canvas, this.hackerIcon!, "top", node.y + offsetY, time, easing)
    }

    animateMoveStepLine(fromNodeDisplay: Display, toNodeDisplay: Display, durationTicks: number, you: boolean, ease: IUtilAminEaseFunction= easeInOutSine): ConnectionVisual {
        const lineData: LinePositions = calcLine(fromNodeDisplay, toNodeDisplay, 0)
        const lineStart = new LinePositions(lineData.line[0], lineData.line[1], lineData.line[0], lineData.line[1])

        const opacity = (you) ? 1: 0.5

        const lineElement = new ConnectionVisual(lineStart, COLOR_HACKER_LINE, this.canvas, {opacity: opacity})
        lineElement.extendTo(lineData, durationTicks, ease)

        return lineElement
    }

    hackerProbeLayers(timing: Timings) {
        this.schedule.run(timing.start, () => {
            this.animateZoom(SCALE_SMALL, timing.start)
        })
        this.schedule.run(timing.end, () => {
            this.animateZoom(SCALE_NORMAL, timing.end)
        })
    }

    animateZoom(scale: number, duration: number) {
        animate(this.canvas, this.hackerIcon!, 'scaleX', scale, duration)
        animate(this.canvas, this.hackerIcon!, 'scaleY', scale, duration)
    }

    animateOpacity(opacity: number, time: number, easing: IUtilAminEaseFunction | null = null) {
        animate(this.canvas, this.hackerIcon!, 'opacity', opacity, time, easing)
    }

    terminate() {
        this.schedule.terminate()
    }

    disconnect() {
        const ticks = 20

        if (this.moveLineElement) {this.moveLineElement.disappear(ticks)}
        this.gfx.fadeOutAndRemove(ticks, this.hackerIcon!)
        this.gfx.fade(ticks, IDENTIFIER_OPACITY_SCANNING, this.hackerIdentifierIcon)
        this.gfx.fade(ticks, LINE_OPACITY_SCANNING, this.labelIcon)
        this.gfx.fade(ticks, LINE_OPACITY_SCANNING, this.startLineIcon)


    }
}
