import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {JigsawEnterData} from "../JigsawServerActionProcessor";
import {generatePieceConfigs} from "../component/JigsawShapes";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";

class JigsawCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    imageLoaded_: boolean = false
    piecesByLocation: { [key: string]: JigsawPieceDisplay } = {}
    pieceSize: number = 0

    // Track the position at the start of a drag so we can compute the delta for group movement
    dragStartLeft: number = 0
    dragStartTop: number = 0

    imageLoaded() {
        this.imageLoaded_ = true
    }

    init(data: JigsawEnterData, dispatch: Dispatch, store: Store) {
        if (!this.imageLoaded_) {
            setTimeout(() => this.init(data, dispatch, store), 100)
            return
        }
        this.dispatch = dispatch
        this.store = store

        const canvasWidth = 900
        const canvasHeight = 700

        this.canvas = new fabric.Canvas('jigsawCanvas', {
            width: canvasWidth,
            height: canvasHeight,
            backgroundColor: "#1a1a2e",
            selection: false,
        })

        const puzzleCols = data.gridSize
        const puzzleRows = data.gridSize

        // Calculate piece size to fit within canvas with some margin
        const maxPieceSize = Math.min(
            (canvasWidth - 80) / (puzzleCols + 1),
            (canvasHeight - 80) / (puzzleRows + 1)
        )
        const pieceSize = Math.min(maxPieceSize, 150)
        this.pieceSize = pieceSize

        const sourceImg = document.getElementById('jigsawSourceImage') as HTMLImageElement
        const pieces = generatePieceConfigs(puzzleCols, puzzleRows)

        this.piecesByLocation = {}
        for (const config of pieces) {
            const display = new JigsawPieceDisplay(
                this.canvas, config.col, config.row, config, sourceImg,
                puzzleCols, puzzleRows, pieceSize, canvasWidth, canvasHeight
            )
            this.piecesByLocation[`${config.col}:${config.row}`] = display
        }

        // Record position at drag start for computing group delta
        this.canvas.on('mouse:down', (event) => {
            if (event.target && event.target.data instanceof JigsawPieceDisplay) {
                this.dragStartLeft = event.target.left ?? 0
                this.dragStartTop = event.target.top ?? 0
            }
        })

        // Move all other pieces in the group along with the dragged piece
        this.canvas.on('object:moving', (event) => {
            if (!event.target || !(event.target.data instanceof JigsawPieceDisplay)) return

            const piece = event.target.data as JigsawPieceDisplay
            if (piece.group.size <= 1) return

            const deltaX = (event.target.left ?? 0) - this.dragStartLeft
            const deltaY = (event.target.top ?? 0) - this.dragStartTop
            this.dragStartLeft = event.target.left ?? 0
            this.dragStartTop = event.target.top ?? 0

            for (const groupPiece of piece.group) {
                if (groupPiece === piece) continue
                groupPiece.moveBy(deltaX, deltaY)
            }
        })

        // After drop, try to snap the whole group to a neighbor
        this.canvas.on('object:modified', (event) => {
            if (event.target && event.target.data instanceof JigsawPieceDisplay) {
                this.trySnapToNeighbor(event.target.data)
            }
        })

        this.canvas.renderAll()
    }

    private trySnapToNeighbor(draggedPiece: JigsawPieceDisplay) {
        const snapTolerance = this.pieceSize * 0.2
        const group = draggedPiece.group

        const neighborOffsets: Array<{ colOffset: number, rowOffset: number }> = [
            {colOffset: -1, rowOffset: 0},
            {colOffset: 1, rowOffset: 0},
            {colOffset: 0, rowOffset: -1},
            {colOffset: 0, rowOffset: 1},
        ]

        // Check every piece in the dragged group for a snap to an outside neighbor
        for (const piece of group) {
            const bodyOrigin = piece.getBodyOrigin()

            for (const {colOffset, rowOffset} of neighborOffsets) {
                const neighborKey = `${piece.col + colOffset}:${piece.row + rowOffset}`
                const neighbor = this.piecesByLocation[neighborKey]
                if (!neighbor || group.has(neighbor)) continue

                const neighborBodyOrigin = neighbor.getBodyOrigin()

                // Where this piece should be, relative to the neighbor
                const expectedX = neighborBodyOrigin.x - colOffset * this.pieceSize
                const expectedY = neighborBodyOrigin.y - rowOffset * this.pieceSize

                const deltaX = Math.abs(bodyOrigin.x - expectedX)
                const deltaY = Math.abs(bodyOrigin.y - expectedY)

                if (deltaX < snapTolerance && deltaY < snapTolerance) {
                    // Compute correction and apply to entire dragged group
                    const correctionX = expectedX - bodyOrigin.x
                    const correctionY = expectedY - bodyOrigin.y

                    for (const groupPiece of group) {
                        groupPiece.moveBy(correctionX, correctionY)
                    }

                    // Merge the two groups
                    draggedPiece.mergeGroup(neighbor)
                    this.canvas.renderAll()
                    return
                }
            }
        }
    }
}

export const jigsawCanvas = new JigsawCanvas()
