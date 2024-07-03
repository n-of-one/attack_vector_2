import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {SweeperCellModifier, SweeperCellType, SweeperImageType} from "../SweeperModel";
import {SweeperModifyAction, SweeperModifyData} from "../SweeperServerActionProcessor";

export const PADDING_TOP = 20
export const PADDING_LEFT = 20


export class SweeperCellDisplay {

    canvas: Canvas

    x: number
    y: number

    imageInfo: SweeperImageInfo

    image: fabric.Image

    size: number

    cellType: SweeperCellType
    modifier: SweeperCellModifier

    constructor(canvas: Canvas, x: number, y: number, cellType: SweeperCellType, modifier: SweeperCellModifier, size: number) {
        this.canvas = canvas

        this.x = x
        this.y = y
        this.cellType = cellType
        this.modifier = modifier
        this.size = size
        this.imageInfo = this.determineImageType()
        const imageElement = this.getHtmlImage(this.imageInfo)
        this.image = this.creatImage(imageElement)

        canvas.add(this.image)
    }


    private creatImage(imageElement: HTMLImageElement): fabric.Image {

        const scale = this.size / this.imageInfo.size
        return new fabric.Image(imageElement, {
            left: PADDING_LEFT + (0.5) * this.size + this.x * this.size,
            top: PADDING_TOP + (0.5) * this.size + this.y * this.size,
            height: this.imageInfo.size,
            width: this.imageInfo.size,
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: true,
            lockMovementY: true,
            selectable: false,
            scaleX: scale,
            scaleY: scale,
            data: this,
            hoverCursor: "pointer",
        })
    }

    private determineImageType(): SweeperImageInfo {

        const imageTypeKey = (this.modifier === SweeperCellModifier.REVEALED)  ? this.cellType.toString() : this.modifier.toString()
        //     return cellType.toString() as SweeperImageType
        // return SweeperImageType[modifier]
        return SWEEPER_IMAGES[imageTypeKey]
    }

    private getHtmlImage(imageInfo: SweeperImageInfo): HTMLImageElement {
        return document.getElementById(imageInfo.id)!! as HTMLImageElement
    }

    updateModifier(newModifier: SweeperCellModifier) {
        this.modifier = newModifier

        this.imageInfo = this.determineImageType()
        const imageElement = this.getHtmlImage(this.imageInfo)
        this.image.setElement(imageElement)
        this.canvas.renderAll();
    }

}



export interface SweeperImageInfo {
    size: number,
    fileName: string,
    id: string
}

export interface SweeperImageTypes {
    [key: string]: SweeperImageInfo
}

export const SWEEPER_IMAGES: SweeperImageTypes = {
    "0": {
        size: 320,
        fileName: "empty.png",
        id: "empty"
    },
    "1": {
        size: 420,
        fileName: "m01-clear.png",
        id: "n1"
    },
    "2": {
        size: 420,
        fileName: "m02-clear.png",
        id: "n2"
    },
    "3": {
        size: 420,
        fileName: "m03-clear.png",
        id: "n3"
    },
    "4": {
        size: 420,
        fileName: "m04-clear.png",
        id: "n4"
    },
    "5": {
        size: 420,
        fileName: "m05-clear.png",
        id: "n5"
    },
    "6": {
        size: 420,
        fileName: "m06-clear.png",
        id: "n6"
    },
    "7": {
        size: 420,
        fileName: "m07-clear.png",
        id: "n7"
    },
    "8": {
        size: 420,
        fileName: "m08-clear.png",
        id: "n8"
    },
    "MINE": {
        size: 420,
        fileName: "gear11.png",
        id: "mine"
    },
    "UNKNOWN": {
        size: 420,
        fileName: "z-roadsign54.png",
        id: "unknown"
    },
    "FLAG": {
        size: 420,
        fileName: "exclamation-point1.png",
        id: "flag"
    },
    "QUESTION_MARK": {
        size: 420,
        fileName: "place-of-interest.png",
        id: "question_mark"
    }
}