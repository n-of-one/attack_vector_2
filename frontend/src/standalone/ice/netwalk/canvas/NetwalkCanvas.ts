import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {NetwalkCellDisplay, PADDING_LEFT, PADDING_TOP} from "./NetwalkCellDisplay";
import {webSocketConnection} from "../../../../common/server/WebSocketConnection";
import {NetwalkCell, CellType, NetwalkRotateUpdate, Point, ServerEnterIceNetwalk} from "../NetwalkServerActionProcessor";
import {ice} from "../../IceModel";

type CellById = { [id: string]: NetwalkCellDisplay }

class NetwalkCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    cellByLocation: CellById = {}

    gridWidth = 0
    gridHeight = 0

    cellSize = 0
    imagesLoaded = 0
    allImagesLoaded = false

    centerCellX = 0
    centerCellY = 0

    imageLoaded(totalImages: number) {
        this.imagesLoaded++
        this.allImagesLoaded = (this.imagesLoaded === totalImages)
    }

    init(data: ServerEnterIceNetwalk, dispatch: Dispatch, store: Store) {
        if (!this.allImagesLoaded) {
            setTimeout(() => {
                this.init(data, dispatch, store)
            }, 100)
            return
        }

        this.dispatch = dispatch;
        this.store = store;

        this.cellSize = this.determineCellSize(data.cellGrid.length)

        this.gridWidth = this.cellSize * data.cellGrid[0].length
        this.gridHeight = this.cellSize * data.cellGrid.length

        this.canvas = this.createCanvas()

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.selection = false;

        this.canvas.discardActiveObject();
        this.canvas.renderAll();

        this.canvas.on('mouse:down', (event: fabric.IEvent<MouseEvent>) => {
            this.mouseDown(event);
        })

        // This is useful to regenerate the wrapping arrows, it's used to generate the background images.
        // See the code in the NetwalkHome as well.
        // this.drawWrappingArrows(data);


        data.cellGrid.forEach((row: NetwalkCell[], y: number) => {
            row.forEach((minimal: NetwalkCell, x: number) => {
                this.createCell(x, y, minimal)
            })
        })

        this.centerCellX = Math.floor(data.cellGrid[0].length / 2)
        this.centerCellY = Math.floor(data.cellGrid.length / 2)
        const location = `${this.centerCellX}:${this.centerCellY}`

        const actualCenterCell = this.cellByLocation[location]
        this.createCell(this.centerCellX, this.centerCellY, {type: CellType.CENTER, connected: true})
        this.cellByLocation[location] = actualCenterCell

        const backgroundImageUrl = this.determineBackgroundImageName(data)

        setTimeout(() => {
            // fabric.Image.fromURL("/img/tmp/netwalk_wrapping_arrows_17x11.png", (img) => {
            fabric.Image.fromURL(backgroundImageUrl, (img) => {
                img.set({width: this.canvas.width, height: this.canvas.height, originX: 'left', originY: 'top'});
                this.canvas.setBackgroundImage(img, this.canvas.renderAll.bind(this.canvas));
            });
        }, 100);
        this.canvas.renderAll()
    }

    private drawWrappingArrows(data: ServerEnterIceNetwalk) {
        const left = PADDING_LEFT - 2
        const top = PADDING_TOP - 2
        const right = PADDING_LEFT + this.gridWidth + 2
        const bottom = PADDING_TOP + this.gridHeight + 2

        const halfCell = this.cellSize / 2
        for (let cellX = 0; cellX < data.cellGrid[0].length; cellX++) {
            for (let cellY = 0; cellY < data.cellGrid.length; cellY++) {
                let x = left + halfCell + cellX * this.cellSize + 2
                let y = top + halfCell + cellY * this.cellSize + 2

                this.addLine(x, top - 10, x, top)
                this.addLine(x, top - 10, x - 5, top - 10 + 5)
                this.addLine(x, top - 10, x + 5, top - 10 + 5)

                this.addLine(x, bottom, x, bottom + 10)
                this.addLine(x, bottom + 10, x - 5, bottom + 10 - 5)
                this.addLine(x, bottom + 10, x + 5, bottom + 10 - 5)

                this.addLine(left - 10, y, left, y)
                this.addLine(left - 10, y, left - 5, y - 5)
                this.addLine(left - 10, y, left - 5, y + 5)

                this.addLine(right, y, right + 10, y)
                this.addLine(right + 10, y, right + 5, y - 5)
                this.addLine(right + 10, y, right + 5, y + 5)
            }
        }

        // Draw border
        // this.addLine(left, top, right, top)
        // this.addLine(left, bottom, right, bottom)
        // this.addLine(left, top, left, bottom)
        // this.addLine(right, top, right, bottom)

    }

    private addLine(x1: number, y1: number, x2: number, y2: number) {
        const line = new fabric.Line(
            [x1, y1, x2, y2], {
                stroke: "#17563a",
                strokeWidth: 1,
                // strokeDashArray: [4, 4],
                selectable: false,
                // hoverCursor: 'default',
            });
        this.canvas.add(line)
    }

    private createCanvas(): Canvas {
        return new fabric.Canvas('netwalkCanvas', {
            width: this.gridWidth + PADDING_LEFT * 2,
            height: this.gridHeight + PADDING_TOP * 2,
            backgroundColor: "#222",
        });
    }

    download() {
        window.open(this.canvas.toDataURL({format:'png'}))
    }

    mouseDown(event: fabric.IEvent<MouseEvent>) {
        if (event?.target?.data instanceof NetwalkCellDisplay) {
            const cell: NetwalkCellDisplay = event.target.data

            const payload = {iceId: ice.id, x: cell.x, y: cell.y}
            webSocketConnection.send("/av/ice/netwalk/rotate", JSON.stringify(payload))
        }
    }


    private createCell(x: number, y: number, minimal: NetwalkCell) {
        const cell = new NetwalkCellDisplay(this.canvas, x, y, minimal, this.cellSize)
        const location = `${x}:${y}`
        this.cellByLocation[location] = cell
    }


    serverSentNodeRotated(data: NetwalkRotateUpdate) {


        const rotatedLocation = `${data.x}:${data.y}`
        this.cellByLocation[rotatedLocation]!!.rotate()

        const connectedLocations: string[] = data.connected.map((point: Point) => {
            return `${point.x}:${point.y}`
        })

        Object.keys(this.cellByLocation).forEach((location: string) => {
            const connected = connectedLocations.includes(location)
            this.cellByLocation[location]!!.updateConnected(connected)
        })

    }

    private determineCellSize(cellsInRow: number) {
        if (cellsInRow <= 9) return 80
        if (cellsInRow <= 11) return 70
        if (cellsInRow <= 13) return 60
        return 50
    }

    private determineBackgroundImageName(data: ServerEnterIceNetwalk) {
        if (!data.wrapping) return "/img/frontier/ice/netwalk/background/netwalk_full.jpg"

        return  `/img/frontier/ice/netwalk/background/netwalk_wrapping_${data.cellGrid[0].length}x${data.cellGrid.length}.jpg`
    }

    finish() {
        const x = PADDING_LEFT + (0.5) * this.cellSize + this.centerCellX * this.cellSize
        const y = PADDING_TOP + (0.5) * this.cellSize + this.centerCellY * this.cellSize

        const circle1 = new fabric.Circle({left: x, top: y, stroke: '#444', fill: '#17563a', radius: 1, opacity:0.4})
        const circle2 = new fabric.Circle({left: x, top: y, stroke: '#444', fill: '#000', radius: 1, opacity:0.5})
        this.canvas.add(circle2)
        this.canvas.sendToBack(circle2)
        this.canvas.add(circle1)
        this.canvas.sendToBack(circle1)

        circle1.animate('radius', 10000, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: 22 * 1000,
        } )
        setTimeout(() => {
        circle2.animate('radius', 10000, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: 5000,
        } )
        }, 5200)

    }
}

export const netwalkCanvas = new NetwalkCanvas()