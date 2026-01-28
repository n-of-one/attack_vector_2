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
    corner: boolean

    cellType: SweeperCellType
    modifier: SweeperCellModifier
    userBlocked: boolean

    image: fabric.Image
    imageInfo: SweeperImageInfo
    crossFadeImage: fabric.Image | null = null

    constructor(canvas: Canvas, x: number, y: number, cellType: SweeperCellType, modifier: SweeperCellModifier, size: number, userBlocked: boolean, corner: boolean) {
        this.canvas = canvas
        this.gfx = new Graphics(canvas)

        this.x = x
        this.y = y
        this.corner = corner
        this.cellType = cellType
        this.modifier = modifier
        this.size = size
        this.userBlocked = userBlocked
        this.imageInfo = this.determineImageInfo()

        this.image = this.creatImage(1)


        canvas.add(this.image)
    }

    private creatImage(opacity: number): fabric.Image {
        const imageElement = this.getHtmlImage(this.imageInfo)
        const hoverCursor = this.userBlocked ? "not-allowed" : this.imageInfo.cursor

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
            hoverCursor: hoverCursor,
            opacity: opacity
        })

    }

    private determineImageInfo(): SweeperImageInfo {

        const imageTypeKey = (this.modifier === SweeperCellModifier.REVEALED)  ? this.cellType.toString() : this.modifier.toString()
        let imageInfo = SWEEPER_IMAGES[imageTypeKey]
        if (imageInfo !== undefined) {
            if (imageTypeKey === "HIDDEN" && this.corner) {
                imageInfo = SWEEPER_IMAGES["HIDDEN_CORNER"]
            }
            return imageInfo
        }
        throw new Error(`No image found for "${imageTypeKey}"`)
    }

    private getHtmlImage(imageInfo: SweeperImageInfo): HTMLImageElement {
        return document.getElementById(imageInfo.id)!! as HTMLImageElement
    }

    updateModifier(newModifier: SweeperCellModifier) {
        if (this.modifier === newModifier) return
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
        this.image.hoverCursor = userBlocked ? "not-allowed" : this.imageInfo.cursor
    }

    crossFadeToNewImage() {
        const crossFadeTime = 3

        this.crossFadeImage = this.image
        this.canvas.sendToBack(this.crossFadeImage)

        this.imageInfo = this.determineImageInfo()
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

export interface SweeperImageInfos {
    [key: string]: SweeperImageInfo
}

export const SWEEPER_IMAGES: SweeperImageInfos = {
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
        cursor: "default"
    },
    "2": {
        size: 420,
        fileName: "2_cccca0.png",
        id: "n2",
        cursor: "default"
    },
    "3": {
        size: 420,
        fileName: "3_ffc387.png",
        id: "n3",
        cursor: "default"
    },
    "4": {
        size: 420,
        fileName: "4_86a0bf.png",
        id: "n4",
        cursor: "default"
    },
    "5": {
        size: 420,
        fileName: "5_99bf86.png",
        id: "n5",
        cursor: "default"
    },
    "6": {
        size: 420,
        fileName: "6_8565aa.png",
        id: "n6",
        cursor: "default"
    },
    "7": {
        size: 420,
        fileName: "7_aa5c54.png",
        id: "n7",
        cursor: "default"
    },
    "8": {
        size: 420,
        fileName: "8_f7f7f7.png",
        id: "n8",
        cursor: "default"
    },
    "MINE": {
        size: 230,
        fileName: "orange_circled-x4.png",
        id: "mine",
        cursor: "default"
    },
    "HIDDEN": {
        size: 420,
        fileName: "hidden.png",
        id: "unrevealed",
        cursor: "pointer"
    },
    "HIDDEN_CORNER": {
        size: 420,
        fileName: "hidden-corner.png",
        id: "unrevealed_corner",
        cursor: "pointer"
    },
    "FLAG": {
        size: 600,
        fileName: "circled-x4-center.png",
        id: "flag",
        cursor: "pointer"
    },
}
