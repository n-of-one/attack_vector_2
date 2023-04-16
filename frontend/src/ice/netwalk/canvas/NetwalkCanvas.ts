import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {NetwalkCell} from "./NetwalkCell";
import {webSocketConnection} from "../../../common/WebSocketConnection";
import {CellMinimal, CellType, NetwalkRotateUpdate, Point, ServerEnterIceNetwalk} from "../NetwalkServerActionProcessor";


interface Position {
    x: number,
    y: number
}

type CellById = { [id: string]: NetwalkCell }


class NetwalkCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    iceId: string | null = null

    cellByLocation: CellById = {}

    imagesLoaded = 0
    allImagesLoaded = false

    imageLoaded(totalImages: number) {
        this.imagesLoaded++
        this.allImagesLoaded =  (this.imagesLoaded === totalImages)
    }

    init(iceId: string, data: ServerEnterIceNetwalk, dispatch: Dispatch, store: Store) {
        if (!this.allImagesLoaded) {
            setTimeout(() => {
                this.init(iceId, data, dispatch, store)
            }, 100)
            return
        }

        this.iceId = iceId
        this.dispatch = dispatch;
        this.store = store;

        this.canvas = this.createCanvas()

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.selection = false;

        this.canvas.discardActiveObject();
        this.canvas.renderAll();

        this.canvas.on('mouse:down', (event: fabric.IEvent<MouseEvent>) => {
            this.mouseDown(event);
        })

        // setTimeout(() => {
        // }, 2000)

        data.cellGrid.forEach((row: CellMinimal[], y: number) => {
            row.forEach((minimal: CellMinimal, x: number) => {
                this.createCell(x, y, minimal)
            })
        })

        const center = Math.floor(data.cellGrid.length / 2)
        const location = `${center}:${center}`

        const actualCenterCell = this.cellByLocation[location]
        this.createCell(center, center, {type: CellType.CENTER, connected: false})
        this.cellByLocation[location] = actualCenterCell
    }

    private createCanvas(): Canvas {
        return new fabric.Canvas('netwalkCanvas', {
            width: 900,
            height: 900,
            backgroundColor: "#222",
        });
    }


    mouseDown(event: fabric.IEvent<MouseEvent>) {
        if (event?.target?.data instanceof NetwalkCell) {
            const cell:NetwalkCell = event.target.data

            const payload = {iceId: this.iceId, x: cell.x, y: cell.y}
            webSocketConnection.send("/av/ice/netwalk/rotate", JSON.stringify(payload))
        }
    }


    private createCell(x: number, y: number, minimal: CellMinimal) {
        const {imageBaseName, rotation} = this.getImageIdAndRotation(minimal.type)
        const cell = new NetwalkCell(this.canvas, x, y, imageBaseName, rotation, minimal)
        const location = `${x}:${y}`
        this.cellByLocation[location] = cell
    }

    private getImageIdAndRotation(type: CellType) {
        switch (type) {
            case CellType.ESW: //"┳"
                return {imageBaseName: "split", rotation: 180}
            case CellType.NSW://"┫"
                return {imageBaseName: "split", rotation: 270}
            case CellType.NEW://"┻"
                return {imageBaseName: "split", rotation: 0}
            case CellType.NES://"┣"
                return {imageBaseName: "split", rotation: 90}

            case CellType.EW://"━"
                return {imageBaseName: "straight", rotation: 90}
            case CellType.NS://"┃"
                return {imageBaseName: "straight", rotation: 0}

            case CellType.N://"╵"
                return {imageBaseName: "end", rotation: 180}
            case CellType.E://"╶"
                return {imageBaseName: "end", rotation: 270}
            case CellType.S://"╷"
                return {imageBaseName: "end", rotation: 0}
            case CellType.W://"╴"
                return {imageBaseName: "end", rotation: 90}

            case CellType.SW://"┓"
                return {imageBaseName: "corner", rotation: 270}
            case CellType.NW://"┛"
                return {imageBaseName: "corner", rotation: 0}
            case CellType.NE://"┗"
                return {imageBaseName: "corner", rotation: 90}
            case CellType.SE://"┏"
                return {imageBaseName: "corner", rotation: 180}

            case CellType.NESW://"╋"
                return {imageBaseName: "cross", rotation: 0}

            case CellType.CENTER://"*"
                return {imageBaseName: "center", rotation: 0}

            default:
                throw Error("Invalid netwalk cell: " + type)
        }
    }

    serverSentNodeRotated(data: NetwalkRotateUpdate) {


        const rotatedLocation = `${data.x}:${data.y}`
        this.cellByLocation[rotatedLocation]!!.rotate()

        const connectedLocations: string[] = data.connected.map((point: Point) => {
            return `${point.x}:${point.y}`
        })

        Object.keys(this.cellByLocation).forEach((location: string) => {
            const connected =  connectedLocations.includes(location)
            this.cellByLocation[location]!!.updateConnected(connected)
        })

    }


}

export const netwalkCanvas = new NetwalkCanvas()

