import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {JigsawEnterData, PieceGroup} from "../JigsawServerActionProcessor";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";
import {SnapGroupDisplay} from "./SnapGroupDisplay";
import {calculatePieceSize} from "../component/JigsawShapes";

// Set center origin globally so left/top refer to the center of each object.
fabric.Object.prototype.originX = "center"
fabric.Object.prototype.originY = "center"

export const CANVAS_HEIGHT = 928;
export const CANVAS_WIDTH = 1880;



export class JigsawCanvas {

    private readonly canvas: Canvas
    private readonly store: Store
    private readonly dispatch: Dispatch
    private readonly piecesByLocation: Map<string, JigsawPieceDisplay>

    // True while a rotation animation is in progress, to prevent overlapping rotations
    private rotating: boolean = false

    constructor(data: JigsawEnterData, dispatch: Dispatch, store: Store, sourceImage: HTMLImageElement) {
        this.dispatch = dispatch
        this.store = store

        // const puzzleDesignWidth = 1576
        // const puzzleDesignHeight = 828
        const puzzleDesignWidth = sourceImage.width
        const puzzleDesignHeight = sourceImage.height

        // const canvasWidth = window.innerWidth - 30
        const canvasWidth = CANVAS_WIDTH
        // const canvasHeight = 828
        const canvasHeight = CANVAS_HEIGHT

        this.canvas = new fabric.Canvas('jigsawCanvas', {
            width: canvasWidth,
            height: canvasHeight,
            backgroundColor: "#181818",
            selection: false,
            fireRightClick: true,
            stopContextMenu: true,
        })

        const puzzleCols = data.columns
        const puzzleRows = data.rows
        const pieceSize = calculatePieceSize(puzzleDesignWidth, puzzleDesignHeight, puzzleCols, puzzleRows)

        this.piecesByLocation = new Map(
            data.pieces.map(config => {
                const display = new JigsawPieceDisplay({
                    col: config.col, row: config.row, config, sourceImage,
                    puzzleCols, puzzleRows, pieceSize
                })
                return [`${config.col}:${config.row}`, display]
            })
        )

        this.addBackgroundImage(sourceImage, pieceSize * puzzleCols, pieceSize * puzzleRows, canvasWidth, canvasHeight)

        // Create snap groups: multi-piece groups from server data, single-piece groups for the rest.
        const groupedPieces = this.applyGroups(data.groups)
        for (const piece of this.piecesByLocation.values()) {
            if (!groupedPieces.has(piece)) {
                new SnapGroupDisplay(new Set([piece]), this.canvas)
            }
        }

        this.registerEventHandlers()
        this.canvas.renderAll()
    }

    /**
     * Apply pre-existing groups: position pieces relative to anchor, then wrap in snap groups.
     * Returns the set of all pieces that were part of a group.
     */
    private applyGroups(groups: PieceGroup[]): Set<JigsawPieceDisplay> {
        const groupedPieces = new Set<JigsawPieceDisplay>()

        for (const group of groups) {
            if (group.length < 2) continue

            const [anchorCol, anchorRow] = group[0]
            const anchor = this.piecesByLocation.get(`${anchorCol}:${anchorRow}`)
            if (!anchor) continue

            const pieces = new Set<JigsawPieceDisplay>()
            pieces.add(anchor)
            groupedPieces.add(anchor)

            for (let i = 1; i < group.length; i++) {
                const [col, row] = group[i]
                const piece = this.piecesByLocation.get(`${col}:${row}`)
                if (!piece) continue

                piece.positionRelativeTo(anchor)
                pieces.add(piece)
                groupedPieces.add(piece)
            }

            const snapGroup = new SnapGroupDisplay(pieces, this.canvas)
            snapGroup.updateStrokeOpacity(this.piecesByLocation)
        }

        return groupedPieces
    }

    private addBackgroundImage(sourceImage: HTMLImageElement, puzzleWidth: number, puzzleHeight: number,
                               canvasWidth: number, canvasHeight: number) {
        // Draw a tiny version of the image, then scale it up with no smoothing to get chunky pixels.
        const pixelSize = 96
        // const pixelSize = 192
        const aspect = sourceImage.naturalWidth / sourceImage.naturalHeight
        const tinyWidth = Math.round(aspect >= 1 ? pixelSize : pixelSize * aspect)
        const tinyHeight = Math.round(aspect >= 1 ? pixelSize / aspect : pixelSize)

        const tinyCanvas = document.createElement('canvas')
        tinyCanvas.width = tinyWidth
        tinyCanvas.height = tinyHeight
        const tinyCtx = tinyCanvas.getContext('2d')!
        tinyCtx.drawImage(sourceImage, 0, 0, tinyWidth, tinyHeight)

        const pixelCanvas = document.createElement('canvas')
        pixelCanvas.width = puzzleWidth
        pixelCanvas.height = puzzleHeight
        const pixelCtx = pixelCanvas.getContext('2d')!
        pixelCtx.imageSmoothingEnabled = false
        pixelCtx.drawImage(tinyCanvas, 0, 0, puzzleWidth, puzzleHeight)

        const img = new fabric.Image(pixelCanvas as unknown as HTMLImageElement, {
            left: canvasWidth / 2,
            top: canvasHeight / 2,
            opacity: 0.25,
            selectable: false,
            evented: false,
        })
        this.canvas.add(img)
        this.canvas.sendToBack(img)
    }

    private registerEventHandlers() {
        this.canvas.on('mouse:down', (event) => {
            if ((event.e as MouseEvent).button !== 2) return
            const snapGroup = this.getGroupFromTarget(event.target)
            if (snapGroup) this.rotateGroup(snapGroup, true)
        })

        // Bring to front on mouse:up, not mouse:down — bringToFront re-adds the object
        // to the canvas which breaks fabric's active drag if done during mouse:down.
        this.canvas.on('mouse:up', (event) => {
            if ((event.e as MouseEvent).button !== 0) return
            const snapGroup = this.getGroupFromTarget(event.target)
            if (snapGroup) this.canvas.bringToFront(snapGroup.fabricGroup)
        })

        this.canvas.on('mouse:wheel', (event) => {
            event.e.preventDefault()
            const target = this.canvas.findTarget(event.e as MouseEvent, false)
            const snapGroup = this.getGroupFromTarget(target)
            if (!snapGroup) return
            const clockwise = (event.e as WheelEvent).deltaY > 0
            this.rotateGroup(snapGroup, clockwise)
        })

        this.canvas.on('object:modified', (event) => {
            const snapGroup = this.getGroupFromTarget(event.target)
            snapGroup?.trySnap(this.piecesByLocation)
        })
    }

    private getGroupFromTarget(target: fabric.Object | undefined | null): SnapGroupDisplay | null {
        if (!target || !(target.data instanceof SnapGroupDisplay)) return null
        return target.data
    }

    private rotateGroup(snapGroup: SnapGroupDisplay, clockwise: boolean) {
        if (this.rotating) return
        this.rotating = true
        snapGroup.rotate(clockwise, () => {
            this.rotating = false
        })
    }
}
