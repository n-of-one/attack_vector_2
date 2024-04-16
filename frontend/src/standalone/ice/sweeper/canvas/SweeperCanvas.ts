import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {PADDING_LEFT, PADDING_TOP, SweeperCellDisplay} from "./SweeperCellDisplay";
import {SweeperCellModifier, SweeperCellType, SweeperGameState, SweeperImage} from "../SweeperModel";
import {ice} from "../../../StandaloneGlobals";
import {webSocketConnection} from "../../../../common/server/WebSocketConnection";

type CellById = { [id: string]: SweeperCellDisplay }

class SweeperCanvas {

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

    cells: SweeperCellType[][] = []
    modifiers: SweeperCellModifier[][] = []

    imageLoaded(totalImages: number) {
        this.imagesLoaded++
        this.allImagesLoaded = (this.imagesLoaded === totalImages)
    }

    init(data: SweeperGameState, dispatch: Dispatch, store: Store) {
        if (!this.allImagesLoaded) {
            setTimeout(() => {
                this.init(data, dispatch, store)
            }, 100)
            return
        }

        this.cells = data.cells
        this.modifiers = data.modifiers

        this.dispatch = dispatch;
        this.store = store;

        this.cellSize = this.determineCellSize(data.cells[0].length)

        this.gridWidth = this.cellSize * data.cells[0].length
        this.gridHeight = this.cellSize * data.cells.length

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


        data.cells.forEach((row: SweeperCellType[], y: number) => {
            row.forEach((tileStatus: SweeperCellType, x: number) => {
                const cellType = data.cells[y][x]
                const modifier = data.modifiers[y][x]
                this.createCell(x, y, cellType, modifier)
            })
        })


        // const backgroundImageUrl = this.determineBackgroundImageName(data)
        //
        // setTimeout(() => {
        //     // fabric.Image.fromURL("/img/tmp/netwalk_wrapping_arrows_17x11.png", (img) => {
        //     fabric.Image.fromURL(backgroundImageUrl, (img) => {
        //         img.set({width: this.canvas.width, height: this.canvas.height, originX: 'left', originY: 'top'});
        //         this.canvas.setBackgroundImage(img, this.canvas.renderAll.bind(this.canvas));
        //     });
        // }, 100);
        this.canvas.renderAll()
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
            fireRightClick: true,  // <-- enable firing of right click events
            stopContextMenu: true, // <--  prevent context menu from showing
        });
    }

    download() {
        window.open(this.canvas.toDataURL({format: 'png'}))
    }

    mouseDown(event: fabric.IEvent<MouseEvent>) {
        if (event?.target?.data instanceof SweeperCellDisplay) {
            const cell: SweeperCellDisplay = event.target.data
            const leftClick = event.e.button === 1

            const payload = {iceId: ice.id, x: cell.x, y: cell.y, leftClick: leftClick}
            webSocketConnection.send("/ice/sweeper/click", JSON.stringify(payload))
        }
    }

    private createCell(x: number, y: number, cellType: SweeperCellType, modifier: SweeperCellModifier) {
        const sweeperImage = this.determineImage(cellType, modifier)
        const cell = new SweeperCellDisplay(this.canvas, x, y, sweeperImage, this.cellSize)
        const location = `${x}:${y}`
        this.cellByLocation[location] = cell
    }

    private determineImage(cellType: SweeperCellType, modifier: SweeperCellModifier): SweeperImage {
        if (modifier === SweeperCellModifier.REVEALED) {
            return SweeperImage[cellType]
        }
        return SweeperImage[modifier]
    }


    // serverSentNodeRotated(data: NetwalkRotateUpdate) {
    //
    //
    //     const rotatedLocation = `${data.x}:${data.y}`
    //     this.cellByLocation[rotatedLocation]!!.rotate()
    //
    //     const connectedLocations: string[] = data.connected.map((point: Point) => {
    //         return `${point.x}:${point.y}`
    //     })
    //
    //     Object.keys(this.cellByLocation).forEach((location: string) => {
    //         const connected = connectedLocations.includes(location)
    //         this.cellByLocation[location]!!.updateConnected(connected)
    //     })
    //
    // }

    private determineCellSize(cellsInRow: number) {
        if (cellsInRow <= 9) return 80
        if (cellsInRow <= 11) return 70
        if (cellsInRow <= 13) return 60
        return 50
    }

    private determineBackgroundImageName(data: SweeperGameState) {
        return "/img/frontier/ice/netwalk/background/netwalk_full.jpg"
    }

    finish() {
        const x = PADDING_LEFT + (0.5) * this.cellSize + this.centerCellX * this.cellSize
        const y = PADDING_TOP + (0.5) * this.cellSize + this.centerCellY * this.cellSize

        const circle1 = new fabric.Circle({left: x, top: y, stroke: '#444', fill: '#17563a', radius: 1, opacity: 0.4})
        const circle2 = new fabric.Circle({left: x, top: y, stroke: '#444', fill: '#000', radius: 1, opacity: 0.5})
        this.canvas.add(circle2)
        this.canvas.sendToBack(circle2)
        this.canvas.add(circle1)
        this.canvas.sendToBack(circle1)

        circle1.animate('radius', 10000, {
            onChange: this.canvas.renderAll.bind(this.canvas),
            duration: 22 * 1000,
        })
        setTimeout(() => {
            circle2.animate('radius', 10000, {
                onChange: this.canvas.renderAll.bind(this.canvas),
                duration: 5000,
            })
        }, 5200)

    }
}

export const sweeperCanvas = new SweeperCanvas()