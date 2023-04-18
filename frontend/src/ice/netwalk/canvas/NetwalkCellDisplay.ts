import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {NetwalkCell, CellType} from "../NetwalkServerActionProcessor";

export const PADDING_TOP = 20
export const PADDING_LEFT = 20
const IMAGE_SIZE = 160


export class NetwalkCellDisplay {

    canvas: Canvas

    x: number
    y: number
    rotation: number


    imageDisconnected: fabric.Image
    imageConnected: fabric.Image

    connected


    constructor(canvas: Canvas, x: number, y: number, cell: NetwalkCell, size: number){
        this.canvas = canvas

        this.x = x
        this.y = y

        this.connected = cell.connected
        const { rotation, imageName} = this.imageInfo(cell.type)
        this.rotation = rotation

        const htmlImageDisconnected = document.getElementById(imageName)!! as HTMLImageElement
        const htmlImageConnected = document.getElementById(`${imageName}Connected`)!! as HTMLImageElement


        this.imageConnected = this.creatImage(htmlImageConnected, size)
        this.imageDisconnected = this.creatImage(htmlImageDisconnected, size)

        canvas.add(this.imageDisconnected)
        canvas.add(this.imageConnected)

        if (!this.connected) {
            this.imageConnected.opacity =0
        }

    }

    private creatImage(image: HTMLImageElement, size: number): fabric.Image {
        const scale = size / IMAGE_SIZE
        return new fabric.Image(image, {
            left: PADDING_LEFT + (0.5) * size + this.x * size,
            top: PADDING_TOP + (0.5) * size + this.y * size,
            height: IMAGE_SIZE,
            width: IMAGE_SIZE,
            lockRotation: true,
            lockScalingX: true,
            lockScalingY: true,
            lockMovementX: true,
            lockMovementY: true,
            selectable: false,
            angle: this.rotation,
            scaleX: scale,
            scaleY: scale,
            data: this,
            hoverCursor: "pointer",
        })
    }

    rotate(){
        this.rotation = (this.rotation + 90) % 360
        this.animateRotation(this.imageDisconnected)
        this.animateRotation(this.imageConnected)

    }

    animateRotation(image: fabric.Image) {
        const rotationTarget = (this.rotation === 0) ? 360 : this.rotation
        image.animate("angle", rotationTarget, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: 100,
            onComplete: () => {
                image.rotate(this.rotation)
                this.canvas.renderAll()
            }
        })

    }

    updateConnected(newConnected: boolean) {
        if (this.connected === newConnected) return
        const delay = newConnected ? 150: 50
        this.connected = newConnected
        setTimeout(() => {
            if (this.connected) {
                this.imageConnected.animate("opacity", 1, {
                    onChange: this.canvas.renderAll.bind(this.canvas),
                    duration: 100,
                })
            }
            else {
                this.imageConnected.animate("opacity", 0, {
                    onChange: this.canvas.renderAll.bind(this.canvas),
                    duration: 100,
                })
            }
        }, delay)
    }

    private imageInfo(type: CellType) {
        switch (type) {
            case CellType.ESW: //"┳"
                return {imageName: "split", rotation: 180}
            case CellType.NSW://"┫"
                return {imageName: "split", rotation: 270}
            case CellType.NEW://"┻"
                return {imageName: "split", rotation: 0}
            case CellType.NES://"┣"
                return {imageName: "split", rotation: 90}

            case CellType.EW://"━"
                return {imageName: "straight", rotation: 90}
            case CellType.NS://"┃"
                return {imageName: "straight", rotation: 0}

            case CellType.N://"╵"
                return {imageName: "end", rotation: 180}
            case CellType.E://"╶"
                return {imageName: "end", rotation: 270}
            case CellType.S://"╷"
                return {imageName: "end", rotation: 0}
            case CellType.W://"╴"
                return {imageName: "end", rotation: 90}

            case CellType.SW://"┓"
                return {imageName: "corner", rotation: 270}
            case CellType.NW://"┛"
                return {imageName: "corner", rotation: 0}
            case CellType.NE://"┗"
                return {imageName: "corner", rotation: 90}
            case CellType.SE://"┏"
                return {imageName: "corner", rotation: 180}

            case CellType.NESW://"╋"
                return {imageName: "cross", rotation: 0}

            case CellType.CENTER://"*"
                return {imageName: "center", rotation: 0}

            default:
                throw Error("Invalid netwalk cell: " + type)
        }
    }
}
