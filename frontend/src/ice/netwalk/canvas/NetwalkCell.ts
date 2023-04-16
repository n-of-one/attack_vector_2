import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {CellMinimal, CellType} from "../NetwalkServerActionProcessor";

const IMAGE_SIZE = 160
const PADDING_TOP = 15
const PADDING_LEFT = 5
const HALF_SIZE = 40
const TILE_SIZE = 80



export class NetwalkCell {

    canvas: Canvas

    x: number
    y: number
    rotation: number

    cellImage: fabric.Image

    cellType: CellType

    imageDisconnected: HTMLImageElement
    imageConnected: HTMLImageElement

    connected


    constructor(canvas: Canvas, x: number, y: number, imageBaseName: string, imageRotation: number, minimal: CellMinimal){
        this.canvas = canvas

        this.x = x
        this.y = y
        this.rotation = imageRotation
        this.cellType = minimal.type
        this.connected = minimal.connected

        this.imageDisconnected = document.getElementById(imageBaseName)!! as HTMLImageElement
        this.imageConnected = document.getElementById(`${imageBaseName}Connected`)!! as HTMLImageElement

        const image = (this.connected) ? this.imageConnected : this.imageDisconnected

        this.cellImage = new fabric.Image(image, {
            left: PADDING_LEFT + HALF_SIZE + x * TILE_SIZE,
            top:  PADDING_TOP + HALF_SIZE + y * TILE_SIZE,
            height: IMAGE_SIZE,
            width: IMAGE_SIZE,
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: true,
            lockMovementY: true,
            selectable: false,
            angle: this.rotation,
            scaleX: 0.5,
            scaleY: 0.5,
            data: this,
            hoverCursor: (this.cellType === CellType.CENTER) ? "default" : "pointer",
         })

        canvas.add(this.cellImage).renderAll()
    }

    rotate(){
        if (this.cellType === CellType.CENTER) return

        this.rotation = (this.rotation + 90) % 360

        const rotationTarget = (this.rotation === 0) ? 360 : this.rotation
        const duration = 100

        this.cellImage.animate("angle", rotationTarget, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: duration,
            onComplete: () => {
                this.cellImage.rotate(this.rotation)
                this.canvas.renderAll()
            }
        })

        if (Math.random() < 0.5) {
            this.flipConnected()
        }

    }

    flipConnected(){
        this.connected = !this.connected

        const image = (this.connected) ? this.imageConnected : this.imageDisconnected
        this.cellImage.setElement(image)
    }

    updateConnected(newConnected: boolean) {
        this.connected = newConnected
        const image = (this.connected) ? this.imageConnected : this.imageDisconnected
        this.cellImage.setElement(image)
    }


}