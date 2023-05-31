import {fabric} from "fabric";
import {BLOCK_HEIGHT, BLOCK_PADDING_BOTTOM, BLOCK_PADDING_LEFT, BLOCK_WIDTH, PADDING_LEFT, PADDING_TOP} from "./SlowIceCanvas";

const SLOW_ICE_FILL_COLOR = "#337ab7"

export class SlowIceBlockDisplay {

    canvas: fabric.Canvas

    left: number
    top: number

    fillBox: fabric.Rect
    outlineBoxBase: fabric.Rect
    outlineBoxAccent: fabric.Rect

    constructor(x: number, y: number, fillFractionInput: number, canvas: fabric.Canvas) {
        this.canvas = canvas

        this.left = PADDING_LEFT + x * (BLOCK_WIDTH + BLOCK_PADDING_LEFT)
        this.top = PADDING_TOP + y * (BLOCK_HEIGHT + BLOCK_PADDING_BOTTOM)

        const completelyFilled = (fillFractionInput >= 1)
        const fillFraction = (completelyFilled) ? 1 : fillFractionInput

        // if (fillFraction === 1) {
        //     this.fillBox = this.createRect(1, SLOW_ICE_FILL_COLOR, 1, '#999')
        //     const outlineBox2 = this.createRect(1, "#0000", 1, '#889', [2, 2])
        //     this.canvas.add(this.fillBox)
        //     this.canvas.add(outlineBox2)
        //     return
        // }

        const strokeColorBase = (completelyFilled) ? '#999' : '#666'
        const strokeColorAccent = (completelyFilled) ? '#889' : '#006'

        const backgroundBox = this.createRect(1, "#0008", 0)
        this.fillBox = this.createRect(fillFraction, SLOW_ICE_FILL_COLOR, 0)
        this.outlineBoxBase = this.createRect(1, "#0000", 1, strokeColorBase)
        this.outlineBoxAccent = this.createRect(1, "#0000", 1, strokeColorAccent, [2, 2])

        this.canvas.add(backgroundBox)
        this.canvas.add(this.fillBox)
        this.canvas.add(this.outlineBoxBase)
        this.canvas.add(this.outlineBoxAccent)
    }

    createRect(fillFraction: number, fillColor: string, strokeWidth: number, strokeColor: string = '#aaa', strokeDashArray: number[] | undefined = undefined) {

        // const top = this.top + (1 - fillFraction) * BLOCK_HEIGHT
        // const height = fillFraction * BLOCK_HEIGHT

        // const left = this.left + (1 - fillFraction) * BLOCK_WIDTH
        const width = fillFraction * BLOCK_WIDTH

        return new fabric.Rect({
            left: this.left,
            top: this.top,
            fill: fillColor,
            width: width,
            height: BLOCK_HEIGHT,
            stroke: strokeColor,
            strokeWidth: strokeWidth,
            strokeDashArray: strokeDashArray,
            rx: 1,
            ry: 1,
            selectable: false,
            hoverCursor: "default",
        })
    }

    updateFillFraction(fillFractionInput: number) {
        const fillFraction = fillFractionInput >= 1 ? 1 : fillFractionInput

        this.fillBox.set({
            // top: this.top + (1 - fillFraction) * BLOCK_HEIGHT,
            // height: fillFraction * BLOCK_HEIGHT,
            // left: this.left + (1 - fillFraction) * BLOCK_WIDTH,
            width: fillFraction * BLOCK_WIDTH,
        })

        if (fillFraction === 1) {
            this.outlineBoxBase.set({
                stroke: '#999',
            })
            this.canvas.remove(this.outlineBoxAccent)
            this.outlineBoxAccent = this.createRect(1, "#0000", 1, '#889', [2, 2])
            this.canvas.add(this.outlineBoxAccent)
        }

        this.canvas.renderAll()
    }
}