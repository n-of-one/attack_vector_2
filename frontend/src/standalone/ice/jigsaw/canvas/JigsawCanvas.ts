import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {JigsawEnterData} from "../JigsawServerActionProcessor";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";
import {calculatePieceSize} from "../component/JigsawShapes";

const SNAP_TOLERANCE_PIXELS = 15

const NEIGHBOR_OFFSETS: ReadonlyArray<{ colOffset: number, rowOffset: number }> = [
    {colOffset: -1, rowOffset: 0},
    {colOffset: 1, rowOffset: 0},
    {colOffset: 0, rowOffset: -1},
    {colOffset: 0, rowOffset: 1},
]

/** Precomputed cos/sin for the four valid rotation angles (avoids floating-point drift). */
const PRECOMPUTED_ROTATION_TRIGONOMETRY: { [degrees: number]: { cos: number, sin: number } } = {
    0: {cos: 1, sin: 0},
    90: {cos: 0, sin: 1},
    180: {cos: -1, sin: 0},
    270: {cos: 0, sin: -1},
}

export class JigsawCanvas {

    private readonly canvas: Canvas
    private readonly store: Store
    private readonly dispatch: Dispatch
    private readonly piecesByLocation: Map<string, JigsawPieceDisplay>
    private readonly pieceSize: number

    // True while a rotation animation is in progress, to prevent overlapping rotations
    private rotating: boolean = false

    constructor(data: JigsawEnterData, dispatch: Dispatch, store: Store, sourceImage: HTMLImageElement) {
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
        this.pieceSize = calculatePieceSize(canvasWidth, canvasHeight, puzzleCols, puzzleRows)

        this.piecesByLocation = new Map(
            data.pieces.map(config => {
                const display = new JigsawPieceDisplay({
                    col: config.col, row: config.row, config, sourceImage,
                    puzzleCols, puzzleRows, pieceSize: this.pieceSize
                })
                return [`${config.col}:${config.row}`, display]
            })
        )

        // Add all pieces as standalone canvas objects
        for (const piece of this.piecesByLocation.values()) {
            this.canvas.add(piece.displayObject)
        }

        this.applyGroups(data.groups)
        this.registerEventHandlers()
        this.canvas.renderAll()
    }

    /**
     * Apply pre-existing groups: merge pieces into shared groups, position them
     * correctly relative to the anchor, then wrap in snap fabric groups.
     */
    private applyGroups(groups: JigsawEnterData['groups']) {
        for (const group of groups) {
            if (group.length < 2) continue

            const [anchorCol, anchorRow] = group[0]
            const anchor = this.piecesByLocation.get(`${anchorCol}:${anchorRow}`)
            if (!anchor) continue

            const anchorCenter = anchor.getBodyCenter()
            const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[anchor.rotation]

            for (let i = 1; i < group.length; i++) {
                const [col, row] = group[i]
                const piece = this.piecesByLocation.get(`${col}:${row}`)
                if (!piece) continue

                // Match rotation to anchor
                piece.rotation = anchor.rotation
                piece.displayObject.set({angle: anchor.rotation})

                // Position relative to anchor using rotated grid offset
                const colOffset = col - anchorCol
                const rowOffset = row - anchorRow
                const canvasOffsetX = (colOffset * cos - rowOffset * sin) * this.pieceSize
                const canvasOffsetY = (colOffset * sin + rowOffset * cos) * this.pieceSize

                piece.setBodyCenter(anchorCenter.x + canvasOffsetX, anchorCenter.y + canvasOffsetY)

                // Merge into anchor's group
                anchor.mergeGroup(piece)
            }

            this.updateStrokeOpacity(anchor.group)
            this.createSnapFabricGroup(anchor.group)
        }
    }

    /** Create a snap fabric.Group from pieces whose displayObjects are already positioned on the canvas. */
    private createSnapFabricGroup(pieces: Set<JigsawPieceDisplay>) {
        if (pieces.size <= 1) return

        // Remove standalone displayObjects from canvas
        for (const piece of pieces) {
            this.canvas.remove(piece.displayObject)
        }

        const displayObjects = [...pieces].map(p => p.displayObject)
        const representative = [...pieces][0]

        const snapGroup = new fabric.Group(displayObjects, {
            selectable: true,
            hasControls: false,
            hasBorders: false,
            lockRotation: true,
            hoverCursor: 'grab',
            subTargetCheck: false,
            perPixelTargetFind: true,
            data: representative,
        })

        for (const piece of pieces) {
            piece.snapFabricGroup = snapGroup
        }

        this.canvas.add(snapGroup)
    }

    /**
     * Merge two snap groups (or standalone pieces) into a single snap fabric.Group.
     * The logical groups must already be merged (via mergeGroup) before calling this.
     */
    private mergeSnapFabricGroups(allPieces: Set<JigsawPieceDisplay>, representative: JigsawPieceDisplay) {
        // Record absolute body centers before destroying groups
        const positions = new Map<JigsawPieceDisplay, { x: number, y: number }>()
        for (const piece of allPieces) {
            positions.set(piece, piece.getBodyCenter())
        }

        // Remove current canvas objects (snap groups or standalone displayObjects)
        const removed = new Set<fabric.Object>()
        for (const piece of allPieces) {
            const obj = piece.getCanvasObject()
            if (!removed.has(obj)) {
                this.canvas.remove(obj)
                removed.add(obj)
            }
        }

        // Extract displayObjects from old snap groups
        for (const piece of allPieces) {
            if (piece.snapFabricGroup) {
                piece.snapFabricGroup.remove(piece.displayObject)
                piece.snapFabricGroup = null
            }
        }

        // Restore absolute positions on now-standalone displayObjects
        for (const piece of allPieces) {
            const pos = positions.get(piece)!
            piece.displayObject.set({angle: piece.rotation})
            piece.setBodyCenter(pos.x, pos.y)
        }

        // Create the merged snap group
        const displayObjects = [...allPieces].map(p => p.displayObject)

        const snapGroup = new fabric.Group(displayObjects, {
            selectable: true,
            hasControls: false,
            hasBorders: false,
            lockRotation: true,
            hoverCursor: 'grab',
            subTargetCheck: false,
            perPixelTargetFind: true,
            data: representative,
        })

        for (const piece of allPieces) {
            piece.snapFabricGroup = snapGroup
        }

        this.canvas.add(snapGroup)
    }

    private registerEventHandlers() {
        // Right-click rotation and z-order on mouse down
        this.canvas.on('mouse:down', (event) => {
            const piece = this.getPieceFromTarget(event.target)
            if (!piece) return

            if ((event.e as MouseEvent).button === 2) {
                this.rotateGroup(piece, true)
                return
            }

            // Bring the canvas object (snap group or standalone piece) to top
            this.canvas.bringToFront(piece.getCanvasObject())
        })

        // Scroll wheel rotates the piece under the cursor
        this.canvas.on('mouse:wheel', (event) => {
            event.e.preventDefault()
            const target = this.canvas.findTarget(event.e as MouseEvent, false)
            const piece = this.getPieceFromTarget(target)
            if (!piece) return
            const clockwise = (event.e as WheelEvent).deltaY > 0
            this.rotateGroup(piece, clockwise)
        })

        // No object:moving handler needed — fabric.Group handles group drag natively

        // After drop, try to snap to a neighbor
        this.canvas.on('object:modified', (event) => {
            const piece = this.getPieceFromTarget(event.target)
            if (piece) {
                this.trySnapToNeighbor(piece)
            }
        })
    }

    private getPieceFromTarget(target: fabric.Object | undefined | null): JigsawPieceDisplay | null {
        if (!target || !(target.data instanceof JigsawPieceDisplay)) return null
        return target.data as JigsawPieceDisplay
    }

    private rotateGroup(clickedPiece: JigsawPieceDisplay, clockwise: boolean) {
        if (this.rotating) return
        this.rotating = true

        const canvasObj = clickedPiece.getCanvasObject()
        const angleDelta = clockwise ? 90 : -90
        const startAngle = canvasObj.angle ?? 0
        const targetAngle = startAngle + angleDelta

        // Pivot: average body center of all pieces in the group (ignoring tabs)
        const pieces = [...clickedPiece.group]
        const centers = pieces.map(p => p.getBodyCenter())
        const pivotX = centers.reduce((sum, c) => sum + c.x, 0) / pieces.length
        const pivotY = centers.reduce((sum, c) => sum + c.y, 0) / pieces.length

        // Rotate the canvas object's bounding-box center around the pivot
        const startCenter = canvasObj.getCenterPoint()
        const dx = startCenter.x - pivotX
        const dy = startCenter.y - pivotY
        // CW 90°: (dx,dy) → (-dy, dx).  CCW 90°: (dx,dy) → (dy, -dx).
        const targetCenterX = clockwise ? pivotX - dy : pivotX + dy
        const targetCenterY = clockwise ? pivotY + dx : pivotY - dx

        canvasObj.animate('angle', targetAngle, {
            onChange: () => {
                // Derive progress from the current angle so position follows the same easing curve
                const currentAngle = canvasObj.angle ?? startAngle
                const progress = (currentAngle - startAngle) / angleDelta
                const currentCenterX = startCenter.x + (targetCenterX - startCenter.x) * progress
                const currentCenterY = startCenter.y + (targetCenterY - startCenter.y) * progress
                canvasObj.setPositionByOrigin(
                    new fabric.Point(currentCenterX, currentCenterY), 'center', 'center'
                )
                canvasObj.setCoords()
                this.canvas.renderAll()
            },
            duration: 200,
            onComplete: () => {
                canvasObj.set({angle: targetAngle})
                canvasObj.setPositionByOrigin(
                    new fabric.Point(targetCenterX, targetCenterY), 'center', 'center'
                )
                canvasObj.setCoords()

                for (const piece of clickedPiece.group) {
                    piece.rotation = (piece.rotation + angleDelta + 360) % 360
                }
                this.canvas.renderAll()
                this.rotating = false
            }
        })
    }

    private trySnapToNeighbor(draggedPiece: JigsawPieceDisplay) {
        const group = draggedPiece.group

        for (const piece of group) {
            const bodyCenter = piece.getBodyCenter()
            const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[piece.rotation]

            for (const {colOffset, rowOffset} of NEIGHBOR_OFFSETS) {
                const neighborKey = `${piece.col + colOffset}:${piece.row + rowOffset}`
                const neighbor = this.piecesByLocation.get(neighborKey)
                if (!neighbor || group.has(neighbor)) continue

                // Only snap pieces that share the same rotation
                if (neighbor.rotation !== piece.rotation) continue

                const neighborBodyCenter = neighbor.getBodyCenter()

                // Rotate the puzzle-space offset into canvas space
                const canvasOffsetX = (colOffset * cos - rowOffset * sin) * this.pieceSize
                const canvasOffsetY = (colOffset * sin + rowOffset * cos) * this.pieceSize

                const expectedX = neighborBodyCenter.x - canvasOffsetX
                const expectedY = neighborBodyCenter.y - canvasOffsetY

                const deltaX = Math.abs(bodyCenter.x - expectedX)
                const deltaY = Math.abs(bodyCenter.y - expectedY)

                if (deltaX < SNAP_TOLERANCE_PIXELS && deltaY < SNAP_TOLERANCE_PIXELS) {
                    const correctionX = expectedX - bodyCenter.x
                    const correctionY = expectedY - bodyCenter.y

                    // Move the entire canvas object by the correction
                    const canvasObj = draggedPiece.getCanvasObject()
                    canvasObj.set({
                        left: (canvasObj.left ?? 0) + correctionX,
                        top: (canvasObj.top ?? 0) + correctionY,
                    })
                    canvasObj.setCoords()

                    // Merge logical groups then create combined snap fabric group
                    draggedPiece.mergeGroup(neighbor)
                    this.mergeSnapFabricGroups(draggedPiece.group, draggedPiece)
                    this.updateStrokeOpacity(draggedPiece.group)
                    this.canvas.renderAll()
                    return
                }
            }
        }
    }

    private updateStrokeOpacity(group: Set<JigsawPieceDisplay>) {
        for (const piece of group) {
            const snappedNeighborCount = NEIGHBOR_OFFSETS.filter(({colOffset, rowOffset}) => {
                const neighbor = this.piecesByLocation.get(`${piece.col + colOffset}:${piece.row + rowOffset}`)
                return neighbor && group.has(neighbor)
            }).length
            piece.updateSnappedNeighborCount(snappedNeighborCount)
        }
    }
}
