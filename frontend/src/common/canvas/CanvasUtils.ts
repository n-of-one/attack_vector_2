import {fabric} from "fabric"
import {Canvas, IUtilAminEaseFunction} from "fabric/fabric-impl"
import {Display} from "./display/Display"
import {TICK_MILLIS} from "../util/Schedule";


export const animate = (canvas: Canvas, toAnimate: fabric.Object, attribute: string | null, value: any, durationTicks: number, easing: IUtilAminEaseFunction | null = null) => {
    if (!toAnimate) return

    const easingFunction = (easing) ? easing : fabric.util.ease.easeInOutSine
    if (attribute) {
        toAnimate.animate(attribute, value, {
            onChange: canvas.renderAll.bind(canvas),
            duration: (durationTicks * TICK_MILLIS) - 25,
            easing: easingFunction
        })
    }
    else {
        toAnimate.animate(value, {
            onChange: canvas.renderAll.bind(canvas),
            duration: (durationTicks * TICK_MILLIS) - 25,
            easing: easingFunction
        })
    }
}


export class LinePositions {
    line: number[]
    
    constructor(x1: number, y1: number, x2: number, y2: number) {
        this.line = [x1, y1, x2, y2]
    }

    asArray()  {
        return this.line
    }

    asCoordinates() {
        return   {
            x1: this.line[0],
            y1: this.line[1],
            x2: this.line[2],
            y2: this.line[3]
        }
    }
}

interface Distance {
    xSpan: number, 
    ySpan: number, 
    distance: number
}

const calcDistance = (from: Display, to: Display): Distance => {
    const xSpan = to.x - from.x
    const ySpan = to.y - from.y
    const distance = Math.sqrt(xSpan * xSpan + ySpan * ySpan)

    return {xSpan: xSpan, ySpan: ySpan, distance: distance}
}

export const calcLine = (from: Display, to: Display, padding: number | null = null) => {
    const fromOffset = from.size
    const toOffset = to.size

    return calcLineWithOffset(from, to, fromOffset, toOffset, padding)
}

export const calcLineWithOffset = (from: Display, to: Display, fromOffset: number, toOffset: number, padding: number | null = null): LinePositions => {
    const {xSpan, ySpan, distance} = calcDistance(from, to)

    const [xPadding, yPadding] = expandPadding(xSpan, ySpan, padding)

    const startRatio = fromOffset / distance
    const finishRatio = (distance - toOffset) / distance

    const x1 = Math.floor(from.x + xSpan * startRatio) + xPadding
    const y1 = Math.floor(from.y + ySpan * startRatio) + yPadding
    const x2 = Math.floor(from.x + xSpan * finishRatio) + xPadding
    const y2 = Math.floor(from.y + ySpan * finishRatio) + yPadding

    return new LinePositions(x1, y1, x2, y2)
}

const expandPadding = (xSpan: number, ySpan: number, padding: number | null): number[] => {
    if (!padding) {
        return [0, 0]
    }

    const dy = ySpan / ((xSpan === 0) ? 1 : xSpan)
    const sign = Math.sign(dy)
    const dyAbs = Math.abs(dy)
    if (dyAbs < 0.5) {
        return [0, padding]
    }
    else if (dyAbs > 5) {
        return [padding, 0]
    }
    else {
        if (sign === 1) {
            return [-padding, padding]
        }
        else {
            return [padding, padding]
        }
    }
}

export function easeLinear (t: number, b: number, c: number, d: number) {
    return b + (t/d) * c
}

export const easeInSine = fabric.util.ease.easeInSine
export const easeOutSine = fabric.util.ease.easeOutSine
export const easeInOutSine = fabric.util.ease.easeInOutSine

export const getHtmlImage = (id: string): HTMLImageElement =>  {
    const image = document.getElementById(id)
    if (image === null || image === undefined) throw new Error("Hacker icon not found by id: " + id)
    if (!(image instanceof HTMLImageElement)) throw new Error("Hacker icon not an id: " + id)
    return image
}