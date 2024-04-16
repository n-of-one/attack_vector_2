import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {SweeperImage} from "../SweeperModel";
import {CellType} from "../../netwalk/NetwalkServerActionProcessor";
import {SWEEPER_IMAGES} from "../component/SweeperHome";

export const PADDING_TOP = 20
export const PADDING_LEFT = 20


export class SweeperCellDisplay {

    canvas: Canvas

    x: number
    y: number

    image: fabric.Image

    sweeperImage: SweeperImage
    size: number

    constructor(canvas: Canvas, x: number, y: number, sweeperImage: SweeperImage, size: number) {
        this.canvas = canvas

        this.x = x
        this.y = y
        this.sweeperImage = sweeperImage
        this.size = size

        this.image = this.creatImage(this.getHtmlImage(), size)

        canvas.add(this.image)
    }

    private getHtmlImage(): HTMLImageElement {
        const imageId = this.determineImageId()
        return document.getElementById(imageId)!! as HTMLImageElement
    }

    private creatImage(image: HTMLImageElement, size: number): fabric.Image {
        const imageInfo = SWEEPER_IMAGES[this.sweeperImage]
        const scale = size / imageInfo.size
        return new fabric.Image(image, {
            left: PADDING_LEFT + (0.5) * size + this.x * size,
            top: PADDING_TOP + (0.5) * size + this.y * size,
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
            hoverCursor: "pointer",
        })
    }


    private determineImageId(): string {
        const id = SWEEPER_IMAGES[this.sweeperImage].id
        if (id === undefined) {
            throw Error("Invalid sweeper cell: " + this.sweeperImage)
        }
        return id
    }

    setImage(sweeperImage: SweeperImage) {
        this.sweeperImage = sweeperImage
        this.updateImage();
    }

    private updateImage() {
        const image = this.getHtmlImage()
        this.image.setElement(image)
        this.canvas.renderAll();
    }

}
