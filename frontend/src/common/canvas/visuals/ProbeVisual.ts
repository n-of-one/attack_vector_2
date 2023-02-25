import {fabric} from "fabric"
import {Canvas} from "fabric/fabric-impl"
import {animate, getHtmlImage} from "../CanvasUtils"
import {Schedule, TICK_MILLIS} from "../../Schedule"
import {NodeDisplay} from "../display/NodeDisplay"
import {Timings} from "../../model/Ticks"

const PROBE_IMAGE_SIZE = 40
const SIZE_MEDIUM = (40 / 40)
const SIZE_SMALL = (20 / 40)
const SIZE_SMALL_MEDIUM = (30 / 40)
const SIZE_MEDIUM_LARGE = (58 / 40)
const SIZE_LARGE = (80 / 40)

const DISAPPEAR_LINE_START_EARLY = 10

export class ProbeVisual {

    canvas: Canvas
    image: fabric.Image
    schedule: Schedule


    constructor(canvas: Canvas, nodeDisplay: NodeDisplay, schedule: Schedule) {

        this.canvas = canvas
        this.schedule = schedule

        const imageNumber = Math.floor(Math.random() * 10) + 1
        const htmlImage = getHtmlImage("PROBE_" + imageNumber)

        this.image = new fabric.Image(htmlImage, {
            left: nodeDisplay.x + 5,
            top: nodeDisplay.y + 5,
            height: PROBE_IMAGE_SIZE,
            width: PROBE_IMAGE_SIZE,
            scaleX: SIZE_MEDIUM,
            scaleY: SIZE_MEDIUM,
            opacity: 0,
            selectable: false,
        })

        this.canvas.add(this.image)
        this.canvas.bringToFront(this.image)

    }

    zoomInAndOutAndRemove(timings: Timings) {
        this.schedule.run(timings.in, () => {
            console.time("scan")
            this.animateZoom(SIZE_SMALL, timings.in)
            this.animateOpacity(0.9, timings.in)
        })
        this.schedule.run(timings.out - DISAPPEAR_LINE_START_EARLY, () => {
            this.animateZoom(SIZE_SMALL_MEDIUM, timings.out)
            this.animateOpacity(0, timings.out + 10)
        })
        this.scheduleRemove()
    }

    zoomOutAndInAndRemove(timings: Timings) {
        this.schedule.run(timings.out, () => {
            this.animateZoom(SIZE_LARGE, timings.out)
            this.animateOpacity(0.7, timings.out)
        })
        this.schedule.run(timings.in - DISAPPEAR_LINE_START_EARLY, () => {
            this.animateZoom(SIZE_MEDIUM_LARGE, timings.in + 10)
            this.animateOpacity(0, timings.in + 10)
        })
        this.scheduleRemove()
    }

    private animateZoom(scale: number, duration: number) {
        animate(this.canvas, this.image, 'scaleX', scale, duration)
        animate(this.canvas, this.image, 'scaleY', scale, duration)
    }

    private animateOpacity(opacity: number, duration: number) {
        animate(this.canvas, this.image, 'opacity', opacity, duration)
    }

    remove() {
        this.canvas.remove(this.image)
    }

    removeError() {
        this.schedule.run(30, () => {
            animate(this.canvas, this.image, "scaleY", 0, 30)
        })
        this.scheduleRemove()
    }

    scheduleRemove() {
        this.schedule.run(0, () => {
            // This will be triggered early, because of DISAPPEAR_LINE_START_EARLY, So we need
            // an additional wait time.
            setTimeout(() => {
                this.remove()
            }, DISAPPEAR_LINE_START_EARLY * TICK_MILLIS);
        })
    }
}