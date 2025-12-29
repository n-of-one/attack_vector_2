import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {PADDING_LEFT, PADDING_TOP, SweeperCellDisplay} from "./SweeperCellDisplay";
import {SweeperModifyData} from "../SweeperServerActionProcessor";
import {sweeperIceManager} from "../SweeperIceManager";
import {Schedule} from "../../../../common/util/Schedule";
import {shuffleArray} from "../../../../common/util/Util";
import {IceStrength} from "../../../../common/model/IceStrength";
import {SweeperCellModifier, SweeperCellType, SweeperGameState} from "../logic/SweeperLogic";
import {currentUser} from "../../../../common/user/CurrentUser";

type CellById = { [id: string]: SweeperCellDisplay }

class SweeperCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    cellDisplayByLocation: CellById = {}

    sizeX = 0
    sizeY = 0

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

        this.userBlocked = data.blockedUserIds.includes(currentUser.id);
        this.dispatch = dispatch;
        this.store = store;

        this.cellSize = this.determineCellSize(data.cells.length)

        this.sizeX = data.cells[0].length
        this.sizeY = data.cells.length

        this.canvas = this.createCanvas()

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        this.canvas.selection = false;

        this.canvas.discardActiveObject();
        this.canvas.renderAll();

        this.canvas.on('mouse:down', (event: fabric.IEvent<MouseEvent>) => {
            this.mouseDown(event);
                // const x = event.e.pageX
                // const y = event.e.pageY
                // console.log("(" + x + "," + y + ")")
        })

        data.cells.forEach((row: SweeperCellType[], y: number) => {
            row.forEach((cellType: SweeperCellType, x: number) => {
                const modifier = data.modifiers[y][x]
                this.createCell(x, y, cellType, modifier)
            })
        })

        setTimeout(() => {
            fabric.Image.fromURL("/img/frontier/ice/sweeper/sweeper-1.jpg", (img) => {
                img.set({width: this.canvas.width, height: this.canvas.height, originX: 'left', originY: 'top'});
                this.canvas.setBackgroundImage(img, this.canvas.renderAll.bind(this.canvas));
            });
        }, 100);
        this.canvas.renderAll()
    }

    createCanvas():Canvas {
        return new fabric.Canvas('netwalkCanvas', {
            width: this.cellSize * this.sizeX + PADDING_LEFT * 2,
            height: this.cellSize * this.sizeY + PADDING_TOP * 2,
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
        const corner = (x % (this.sizeX - 1) === 0 && y % (this.sizeY - 1) === 0)
        const cellDisplay = new SweeperCellDisplay(this.canvas, x, y, cellType, modifier, this.cellSize, this.userBlocked, corner)
        const location = `${x}:${y}`
        this.cellDisplayByLocation[location] = cellDisplay
    }

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

    serverModified(data:SweeperModifyData, newModifier:SweeperCellModifier) {
        data.cells.forEach((location: string) => { // "$x:$y"
            this.cellDisplayByLocation[location].updateModifier(newModifier)
        })
        this.canvas.renderAll()
    }

    serverSolved(finishFunction: () => void){
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
        schedule.wait(30)
        schedule.run(0, finishFunction)
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

    changeUserBlocked(blocked: boolean) {
        Object.values(this.cellDisplayByLocation).forEach((cellDisplay: SweeperCellDisplay) => {
            cellDisplay.changeUserBlocked(blocked)
        })
    }
}

export const sweeperCanvas = new SweeperCanvas()
