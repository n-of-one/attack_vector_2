import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {animate, getHtmlImage} from "../CanvasUtils";
import {Schedule} from "../../Schedule";
import {NodeDisplay} from "../display/NodeDisplay";
import {Ticks} from "../../model/Ticks";

const PROBE_IMAGE_SIZE = 40;
const SIZE_MEDIUM = (40 / 40);
const SIZE_SMALL = (20 / 40);
const SIZE_SMALL_MEDIUM = (30 / 40);
const SIZE_MEDIUM_LARGE = (58 / 40);
const SIZE_LARGE = (80 / 40);


export class ProbeVisual {

    canvas: Canvas
    image: fabric.Image
    schedule: Schedule


    constructor(canvas: Canvas, nodeDisplay: NodeDisplay, schedule: Schedule) {

        this.canvas = canvas
        this.schedule = schedule

        const imageNumber = Math.floor(Math.random() * 10) + 1;
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
        });

        this.canvas.add(this.image);
        this.canvas.bringToFront(this.image);

    }

    zoomInAndOutAndRemove(ticks: Ticks) {
        this.schedule.run(ticks.in, () => {
            console.time("scan")
            this.animateZoom(SIZE_SMALL, ticks.in)
            this.animateOpacity(0.9, ticks.in)
        })
        this.schedule.run(ticks.out, () => {
            this.animateZoom(SIZE_SMALL_MEDIUM, ticks.out)
            this.animateOpacity(0, ticks.out + 10)
        })
        this.scheduleRemove()
    }

    zoomOutAndInAndRemove(ticks: Ticks) {
        this.schedule.run(ticks.out, () => {
            this.animateZoom(SIZE_LARGE, ticks.out)
            this.animateOpacity(0.7, ticks.out)
        })
        this.schedule.run(ticks.in, () => {
            this.animateZoom(SIZE_MEDIUM_LARGE, ticks.in + 10)
            this.animateOpacity(0, ticks.in + 10)
        })
        this.scheduleRemove()
    }

    private animateZoom(scale: number, duration: number) {
        animate(this.canvas, this.image, 'scaleX', scale, duration);
        animate(this.canvas, this.image, 'scaleY', scale, duration);
    }

    private animateOpacity(opacity: number, duration: number) {
        animate(this.canvas, this.image, 'opacity', opacity, duration);
    }

    remove() {
        this.canvas.remove(this.image);
    }

    removeError() {
        this.schedule.run(30, () => {
            animate(this.canvas, this.image, "scaleY", 0, 30)
        });
        this.scheduleRemove()
    }

    scheduleRemove() {
        this.schedule.run(0, () => {
            this.remove()
        })
    }
}