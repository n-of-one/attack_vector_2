import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {Graphics} from "../../../../common/canvas/display/Display";
import {animate} from "../../../../common/canvas/CanvasUtils";
import {SweeperCellModifier, SweeperCellType} from "../logic/SweeperLogic";
import {delayTicks} from "../../../../common/util/Util";

export const PADDING_TOP = 20
export const PADDING_LEFT = 20


export class SweeperCellDisplay {
    canvas: Canvas
    gfx: Graphics

    x: number
    y: number
    size: number

    cellType: SweeperCellType
    modifier: SweeperCellModifier
    userBlocked: boolean

    image: fabric.Image
    crossFadeImage: fabric.Image | null = null

    constructor(canvas: Canvas, x: number, y: number, cellType: SweeperCellType, modifier: SweeperCellModifier, size: number, userBlocked: boolean) {
        this.canvas = canvas
        this.gfx = new Graphics(canvas)

        this.x = x
        this.y = y
        this.cellType = cellType
        this.modifier = modifier
        this.size = size
        this.userBlocked = userBlocked
        this.image = this.creatImage(1)



        canvas.add(this.image)
    }

    private creatImage(opacity: number): fabric.Image {
        const imageInfo = this.determineImageType()
        const imageElement = this.getHtmlImage(imageInfo)
        const hoverCursor = this.userBlocked ? "not-allowed" : imageInfo.cursor

        const scale = this.size / imageInfo.size
        return new fabric.Image(imageElement, {
            left: PADDING_LEFT + (0.5) * this.size + this.x * this.size,
            top: PADDING_TOP + (0.5) * this.size + this.y * this.size,
            height: imageInfo.size,
            width: imageInfo.size,
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: true,
            lockMovementY: true,
            selectable: false,
            scaleX: scale,
            scaleY: scale,
            data: this,
            hoverCursor: hoverCursor,
            opacity: opacity
        })
    }

    private determineImageType(): SweeperImageInfo {

        const imageTypeKey = (this.modifier === SweeperCellModifier.REVEALED)  ? this.cellType.toString() : this.modifier.toString()
        const image = SWEEPER_IMAGES[imageTypeKey]
        if (image !== undefined) {
            return image
        }
        throw new Error(`No image found for "${imageTypeKey}"`)
    }

    private getHtmlImage(imageInfo: SweeperImageInfo): HTMLImageElement {
        return document.getElementById(imageInfo.id)!! as HTMLImageElement
    }

    updateModifier(newModifier: SweeperCellModifier) {
        this.modifier = newModifier
        this.crossFadeToNewImage()
    }

    fade() {
        new Graphics(this.canvas).fadeOut(40, this.image)
        // @ts-ignore
        animate(this.canvas, this.image, "scaleX", this.image.scaleX * 1.6, 40)
        // @ts-ignore
        animate(this.canvas, this.image, "scaleY", this.image.scaleY * 1.6, 40)
    }

    changeUserBlocked(userBlocked: boolean) {
        this.image.hoverCursor = userBlocked ? "not-allowed" : "pointer"
    }

    revealIfHidden() {
        if (this.modifier !== SweeperCellModifier.FLAG) {
            this.updateModifier(SweeperCellModifier.FLAG)
        }
    }

    crossFadeToNewImage() {
        const crossFadeTime = 3

        this.crossFadeImage = this.image
        this.canvas.sendToBack(this.crossFadeImage)

        this.image = this.creatImage(0)
        this.canvas.add(this.image)

        this.gfx.fade(crossFadeTime, 1, this.image)
        this.gfx.fadeOut(crossFadeTime, this.crossFadeImage)

        delayTicks(crossFadeTime, () => {
            if (!this.crossFadeImage) {
                return
            }
            this.canvas.remove(this.crossFadeImage)
            this.crossFadeImage = null
        })

    }
}

export interface SweeperImageInfo {
    size: number,
    fileName: string,
    id: string,
    cursor: string,
}

export interface SweeperImageTypes {
    [key: string]: SweeperImageInfo
}

export const SWEEPER_IMAGES: SweeperImageTypes = {
    "0": {
        size: 320,
        fileName: "empty_blur.png",
        id: "empty",
        cursor: "default"
    },
    "1": {
        size: 420,
        fileName: "1_888787.png",
        id: "n1",
        cursor: "pointer"
    },
    "2": {
        size: 420,
        fileName: "2_cccca0.png",
        id: "n2",
        cursor: "pointer"
    },
    "3": {
        size: 420,
        fileName: "3_ffc387.png",
        id: "n3",
        cursor: "pointer"
    },
    "4": {
        size: 420,
        fileName: "4_86a0bf.png",
        id: "n4",
        cursor: "pointer"
    },
    "5": {
        size: 420,
        fileName: "5_99bf86.png",
        id: "n5",
        cursor: "pointer"
    },
    "6": {
        size: 420,
        fileName: "6_8565aa.png",
        id: "n6",
        cursor: "pointer"
    },
    "7": {
        size: 420,
        fileName: "7_aa5c54.png",
        id: "n7",
        cursor: "pointer"
    },
    "8": {
        size: 420,
        fileName: "8_f7f7f7.png",
        id: "n8",
        cursor: "pointer"
    },
    "MINE": {
        size: 230,
        fileName: "orange_circled-x4.png",
        id: "mine",
        cursor: "default"
    },
    "HIDDEN": {
        size: 420,
        fileName: "z-roadsign54.png",
        id: "unrevealed",
        cursor: "pointer"
    },
    "FLAG": {
        size: 600,
        fileName: "circled-x4-center.png",
        id: "flag",
        cursor: "pointer"
    },
}