import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {JigsawEnterData} from "../JigsawServerActionProcessor";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";

const SNAP_TOLERANCE_PIXELS = 15
const CANVAS_MARGIN = 80
const MAX_PIECE_SIZE = 150

const NEIGHBOR_OFFSETS: ReadonlyArray<{ colOffset: number, rowOffset: number }> = [
    {colOffset: -1, rowOffset: 0},
    {colOffset: 1, rowOffset: 0},
    {colOffset: 0, rowOffset: -1},
    {colOffset: 0, rowOffset: 1},
]

const PRECOMPUTED_ROTATION_TRIGONOMETRY: { [degrees: number]: { cos: number, sin: number } } = {
    0: {cos: 1, sin: 0},
    90: {cos: 0, sin: 1},
    180: {cos: -1, sin: 0},
    270: {cos: 0, sin: -1},
}

class JigsawCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    imageLoaded_: boolean = false
    piecesByLocation: { [key: string]: JigsawPieceDisplay } = {}
    pieceSize: number = 0
    puzzleCols: number = 0
    puzzleRows: number = 0

    // Track the position at the start of a drag so we can compute the delta for group movement
    dragStartLeft: number = 0
    dragStartTop: number = 0

    // True while a rotation animation is in progress, to prevent overlapping rotations
    rotating: boolean = false

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

        const canvasWidth = 1576
        const canvasHeight = 828

        this.canvas = new fabric.Canvas('jigsawCanvas', {
            width: canvasWidth,
            height: canvasHeight,
            backgroundColor: "#1a1a2e",
            selection: false,
            fireRightClick: true,
            stopContextMenu: true,
        })

        const puzzleCols = data.columns
        const puzzleRows = data.rows
        this.puzzleCols = puzzleCols
        this.puzzleRows = puzzleRows

        // Calculate piece size to fit within canvas with some margin
        const maxPieceSize = Math.min(
            (canvasWidth - CANVAS_MARGIN) / (puzzleCols + 1),
            (canvasHeight - CANVAS_MARGIN) / (puzzleRows + 1)
        )
        const pieceSize = Math.min(maxPieceSize, MAX_PIECE_SIZE)
        this.pieceSize = pieceSize

        const sourceImg = document.getElementById('jigsawSourceImage') as HTMLImageElement

        this.piecesByLocation = {}
        for (const config of data.pieces) {
            const display = new JigsawPieceDisplay({
                canvas: this.canvas, col: config.col, row: config.row, config, sourceImage: sourceImg,
                puzzleCols, puzzleRows, pieceSize, canvasWidth, canvasHeight
            })
            this.piecesByLocation[`${config.col}:${config.row}`] = display
        }

        // Record position at drag start for computing group delta, and handle right-click rotation
        this.canvas.on('mouse:down', (event) => {
            if (!event.target || !(event.target.data instanceof JigsawPieceDisplay)) return

            if ((event.e as MouseEvent).button === 2) {
                this.rotateGroup(event.target.data as JigsawPieceDisplay, true)
                return
            }

            this.dragStartLeft = event.target.left ?? 0
            this.dragStartTop = event.target.top ?? 0
        })

        // Scroll wheel rotates the piece under the cursor: down = CW, up = CCW
        this.canvas.on('mouse:wheel', (event) => {
            event.e.preventDefault()
            const target = this.canvas.findTarget(event.e as MouseEvent, false)
            if (!target || !(target.data instanceof JigsawPieceDisplay)) return
            const clockwise = (event.e as WheelEvent).deltaY > 0
            this.rotateGroup(target.data as JigsawPieceDisplay, clockwise)
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

    private rotateGroup(clickedPiece: JigsawPieceDisplay, clockwise: boolean) {
        if (this.rotating) return
        this.rotating = true

        const group = clickedPiece.group

        // Calculate pivot: average of all body centers in the group
        let pivotX = 0
        let pivotY = 0
        for (const piece of group) {
            const center = piece.getBodyCenter()
            pivotX += center.x
            pivotY += center.y
        }
        pivotX /= group.size
        pivotY /= group.size

        const renderCallback = this.canvas.renderAll.bind(this.canvas)
        let completedCount = 0
        const onPieceComplete = () => {
            completedCount++
            if (completedCount === group.size) {
                this.rotating = false
            }
        }
        for (const piece of group) {
            piece.animateRotation(pivotX, pivotY, clockwise, renderCallback, onPieceComplete)
        }
    }

    private trySnapToNeighbor(draggedPiece: JigsawPieceDisplay) {
        const group = draggedPiece.group

        // Check every piece in the dragged group for a snap to an outside neighbor.
        // The puzzle-space offset (colOffset, rowOffset) must be rotated into canvas space
        // so that snapping works at any rotation. E.g. at 90° CW, "below" in puzzle space
        // becomes "to the left" in canvas space.
        for (const piece of group) {
            const bodyCenter = piece.getBodyCenter()
            const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[piece.rotation]

            for (const {colOffset, rowOffset} of NEIGHBOR_OFFSETS) {
                const neighborKey = `${piece.col + colOffset}:${piece.row + rowOffset}`
                const neighbor = this.piecesByLocation[neighborKey]
                if (!neighbor || group.has(neighbor)) continue

                // Only snap pieces that share the same rotation
                if (neighbor.rotation !== piece.rotation) continue

                const neighborBodyCenter = neighbor.getBodyCenter()

                // Rotate the puzzle-space offset into canvas space
                const canvasOffsetX = (colOffset * cos - rowOffset * sin) * this.pieceSize
                const canvasOffsetY = (colOffset * sin + rowOffset * cos) * this.pieceSize

                // Where this piece's body center should be, relative to the neighbor
                const expectedX = neighborBodyCenter.x - canvasOffsetX
                const expectedY = neighborBodyCenter.y - canvasOffsetY

                const deltaX = Math.abs(bodyCenter.x - expectedX)
                const deltaY = Math.abs(bodyCenter.y - expectedY)

                if (deltaX < SNAP_TOLERANCE_PIXELS && deltaY < SNAP_TOLERANCE_PIXELS) {
                    // Compute correction and apply to entire dragged group
                    const correctionX = expectedX - bodyCenter.x
                    const correctionY = expectedY - bodyCenter.y

                    for (const groupPiece of group) {
                        groupPiece.moveBy(correctionX, correctionY)
                    }

                    // Merge the two groups
                    draggedPiece.mergeGroup(neighbor)
                    this.updateStrokeOpacity(draggedPiece.group)
                    this.canvas.renderAll()
                    return
                }
            }
        }
    }

    private updateStrokeOpacity(group: Set<JigsawPieceDisplay>) {
        for (const piece of group) {
            let snappedNeighborCount = 0
            for (const {colOffset, rowOffset} of NEIGHBOR_OFFSETS) {
                const neighbor = this.piecesByLocation[`${piece.col + colOffset}:${piece.row + rowOffset}`]
                if (neighbor && group.has(neighbor)) {
                    snappedNeighborCount++
                }
            }
            piece.updateSnappedNeighborCount(snappedNeighborCount)
        }
    }
}

export const jigsawCanvas = new JigsawCanvas()
