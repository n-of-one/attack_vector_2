import {fabric} from "fabric";
import {BLOCK_HEIGHT, BLOCK_PADDING_BOTTOM, BLOCK_PADDING_LEFT, BLOCK_WIDTH, PADDING_LEFT, PADDING_TOP} from "./SlowIceCanvas";
import {TICK_MILLIS} from "../../../common/Schedule";
import {easeLinear} from "../../../common/canvas/CanvasUtils";

const SLOW_ICE_FILL_COLOR = "#337ab7"

export class SlowIceBlockDisplay {

    canvas: fabric.Canvas

    left: number
    top: number
    completeFraction: number

    fillBox: fabric.Rect

    constructor(x: number, y: number, completeFraction: number, canvas: fabric.Canvas) {
        this.canvas = canvas

        this.left = PADDING_LEFT + x * (BLOCK_WIDTH + BLOCK_PADDING_LEFT)
        this.top = PADDING_TOP + y * (BLOCK_HEIGHT + BLOCK_PADDING_BOTTOM)

        this.completeFraction = completeFraction

        if (completeFraction >= 1) {
            this.fillBox = this.createRect( this.completeFraction, SLOW_ICE_FILL_COLOR, 2)
            this.canvas.add(this.fillBox)
            return
        }

        const backgroundBox = this.createRect( 1, "#0008", 0)
        this.fillBox = this.createRect( this.completeFraction, SLOW_ICE_FILL_COLOR, 0)
        const outlineBox = this.createRect( 1, "#0000", 2)

        this.canvas.add(backgroundBox)
        this.canvas.add(this.fillBox)
        this.canvas.add(outlineBox)
    }

    createRect(fillFraction: number, fillColor: string, strokeWidth: number = 0) {

        const top = this.top + (1 - fillFraction) * BLOCK_HEIGHT
        const height = fillFraction * BLOCK_HEIGHT

        return new fabric.Rect({
            left: this.left,
            top: top,
            fill: fillColor,
            width: BLOCK_WIDTH,
            height: height,
            stroke: '#aaa',
            strokeWidth: strokeWidth,
            rx: 1,
            ry: 1,
            selectable: false,
            hoverCursor: "default",
        })
    }

    updateFillFraction(fillFraction: number) {
        this.fillBox.set({
            top: this.top + (1 - fillFraction) * BLOCK_HEIGHT,
            height: fillFraction * BLOCK_HEIGHT,
        })
        // this.fillBox.animate({
        //     top: this.top + (1 - fillFraction) * BLOCK_HEIGHT,
        //     height: fillFraction * BLOCK_HEIGHT,
        // }, {
        //     onChange: this.canvas.renderAll.bind(this.canvas),
        //     duration: 10,
        //     easing: easeLinear
        // })

    }
}