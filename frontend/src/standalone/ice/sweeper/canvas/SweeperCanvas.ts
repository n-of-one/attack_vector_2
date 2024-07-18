import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {PADDING_LEFT, PADDING_TOP, SweeperCellDisplay} from "./SweeperCellDisplay";
import {SweeperCellModifier, SweeperCellType, SweeperGameState} from "../SweeperModel";
import {SweeperModifyData} from "../SweeperServerActionProcessor";
import {sweeperIceManager} from "../SweeperIceManager";
import {Schedule} from "../../../../common/util/Schedule";
import {shuffleArray} from "../../../../common/util/Util";
import {IceStrength} from "../../../../common/model/IceStrength";

type CellById = { [id: string]: SweeperCellDisplay }

class SweeperCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    cellDisplayByLocation: CellById = {}

    gridWidth = 0
    gridHeight = 0

    cellSize = 0
    imagesLoaded = 0
    allImagesLoaded = false

    userBlocked = false;

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

        this.userBlocked = data.userBlocked;
        this.dispatch = dispatch;
        this.store = store;

        this.cellSize = this.determineCellSize(data.cells.length)

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
            row.forEach((cellType: SweeperCellType, x: number) => {
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

    // private addLine(x1: number, y1: number, x2: number, y2: number) {
    //     const line = new fabric.Line(
    //         [x1, y1, x2, y2], {
    //             stroke: "#17563a",
    //             strokeWidth: 1,
    //             // strokeDashArray: [4, 4],
    //             selectable: false,
    //             // hoverCursor: 'default',
    //         });
    //     this.canvas.add(line)
    // }


    createCanvas():Canvas {
        return new fabric.Canvas('netwalkCanvas', {
            width: this.gridWidth + PADDING_LEFT * 2,
            height: this.gridHeight + PADDING_TOP * 2,
            backgroundColor: "#222",
            fireRightClick: true,  // <-- enable firing of right click events
            stopContextMenu: true, // <--  prevent context menu from showing
        });
    }

    mouseDown(event:fabric.IEvent<MouseEvent>) {
        if (event?.target?.data instanceof SweeperCellDisplay) {
            const cell: SweeperCellDisplay = event.target.data
            const leftClick = event.e.button === 0

            sweeperIceManager.click(cell.x, cell.y, leftClick)
        }
    }

    createCell(x: number, y: number, cellType: SweeperCellType, modifier: SweeperCellModifier) {
        const cellDisplay = new SweeperCellDisplay(this.canvas, x, y, cellType, modifier, this.cellSize, this.userBlocked)
        const location = `${x}:${y}`
        this.cellDisplayByLocation[location] = cellDisplay
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


    determineCellSize(cellsInColumn:number) {
        switch (cellsInColumn) {
            case 9:
                return 86
            case 12:
                return 66
            case 16:
                return 50
            case 18:
                return 44
            default:
                return 40
        }
    }

    // private determineBackgroundImageName(data: SweeperGameState) {
    //     return "/img/frontier/ice/netwalk/background/netwalk_full.jpg"
    // }

    // finish() {
    //     const x = PADDING_LEFT + (0.5) * this.cellSize + this.centerCellX * this.cellSize
    //     const y = PADDING_TOP + (0.5) * this.cellSize + this.centerCellY * this.cellSize
    //
    //     const circle1 = new fabric.Circle({left: x, top: y, stroke: '#444', fill: '#17563a', radius: 1, opacity: 0.4})
    //     const circle2 = new fabric.Circle({left: x, top: y, stroke: '#444', fill: '#000', radius: 1, opacity: 0.5})
    //     this.canvas.add(circle2)
    //     this.canvas.sendToBack(circle2)
    //     this.canvas.add(circle1)
    //     this.canvas.sendToBack(circle1)
    //
    //     circle1.animate('radius', 10000, {
    //         onChange: this.canvas.renderAll.bind(this.canvas),
    //         duration: 22 * 1000,
    //     })
    //     setTimeout(() => {
    //         circle2.animate('radius', 10000, {
    //             onChange: this.canvas.renderAll.bind(this.canvas),
    //             duration: 5000,
    //         })
    //     }, 5200)
    //
    // }

    serverModified(data:SweeperModifyData, newModifier:SweeperCellModifier) {
        data.cells.forEach((location: string) => { // "$x:$y"
            this.cellDisplayByLocation[location].updateModifier(newModifier)
        })
        this.canvas.renderAll()
    }

    serverSolved() {
        const iceStrength = this.store.getState().ui.strength
        const delay = this.determineDelay(iceStrength)

        const schedule = new Schedule(null)
        const mines = Object.values(this.cellDisplayByLocation)
            .filter((cellDisplay: SweeperCellDisplay) => cellDisplay.cellType === SweeperCellType.MINE)
        shuffleArray(mines)

        mines.forEach((cellDisplay: SweeperCellDisplay) => {
            schedule.run(delay, () => {
                cellDisplay.fade()
            })
        })
    }


    determineDelay(iceStrength:IceStrength): number {
        switch (iceStrength) {
            case IceStrength.VERY_WEAK:
                return 6
            case IceStrength.WEAK:
                return 3
            case IceStrength.AVERAGE:
                return 2
            default:
                return 1
        }
    }
}

export const
    sweeperCanvas = new SweeperCanvas()