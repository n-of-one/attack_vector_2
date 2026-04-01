import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {JigsawEnterData} from "../JigsawServerActionProcessor";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";
import {NEIGHBOR_OFFSETS, SnapGroupDisplay} from "./SnapGroupDisplay";
import {calculatePieceSize} from "../component/JigsawShapes";

// Set center origin globally so left/top refer to the center of each object.
fabric.Object.prototype.originX = "center"
fabric.Object.prototype.originY = "center"

const SNAP_TOLERANCE_PIXELS = 15

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

        // Create snap groups: multi-piece groups from server data, single-piece groups for the rest.
        const groupedPieces = this.applyGroups(data.groups)
        for (const piece of this.piecesByLocation.values()) {
            if (!groupedPieces.has(piece)) {
                SnapGroupDisplay.createForPiece(piece, this.canvas)
            }
        }

        this.registerEventHandlers()
        this.canvas.renderAll()
    }

    /**
     * Apply pre-existing groups: position pieces relative to anchor, then wrap in snap groups.
     * Returns the set of all pieces that were part of a group.
     */
    private applyGroups(groups: JigsawEnterData['groups']): Set<JigsawPieceDisplay> {
        const groupedPieces = new Set<JigsawPieceDisplay>()

        for (const group of groups) {
            if (group.length < 2) continue

            const [anchorCol, anchorRow] = group[0]
            const anchor = this.piecesByLocation.get(`${anchorCol}:${anchorRow}`)
            if (!anchor) continue

            const anchorCenter = anchor.getBodyCenter()
            const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[anchor.rotation]

            const pieces = new Set<JigsawPieceDisplay>()
            pieces.add(anchor)
            groupedPieces.add(anchor)

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
                pieces.add(piece)
                groupedPieces.add(piece)
            }

            const snapGroup = SnapGroupDisplay.createFromPieces(pieces, this.canvas)
            snapGroup.updateStrokeOpacity(this.piecesByLocation)
        }

        return groupedPieces
    }

    private registerEventHandlers() {
        // Right-click rotation and z-order on mouse down
        this.canvas.on('mouse:down', (event) => {
            const snapGroup = this.getGroupFromTarget(event.target)
            if (!snapGroup) return

            if ((event.e as MouseEvent).button === 2) {
                this.rotateGroup(snapGroup, true)
                return
            }

            // Bring the snap group to top
            this.canvas.bringToFront(snapGroup.fabricGroup)
        })

        // Scroll wheel rotates the piece under the cursor
        this.canvas.on('mouse:wheel', (event) => {
            event.e.preventDefault()
            const target = this.canvas.findTarget(event.e as MouseEvent, false)
            const snapGroup = this.getGroupFromTarget(target)
            if (!snapGroup) return
            const clockwise = (event.e as WheelEvent).deltaY > 0
            this.rotateGroup(snapGroup, clockwise)
        })

        // After drop, try to snap to a neighbor
        this.canvas.on('object:modified', (event) => {
            const snapGroup = this.getGroupFromTarget(event.target)
            if (snapGroup) {
                this.trySnapToNeighbor(snapGroup)
            }
        })
    }

    private getGroupFromTarget(target: fabric.Object | undefined | null): SnapGroupDisplay | null {
        if (!target || !(target.data instanceof SnapGroupDisplay)) return null
        return target.data
    }

    private rotateGroup(snapGroup: SnapGroupDisplay, clockwise: boolean) {
        if (this.rotating) return
        this.rotating = true

        const fabricGroup = snapGroup.fabricGroup
        const angleDelta = clockwise ? 90 : -90
        const startAngle = fabricGroup.angle ?? 0
        const targetAngle = startAngle + angleDelta

        // Rotate around the fabric group's own center (bounding box center).
        // No position adjustment needed — the group stays visually in place.
        fabricGroup.animate('angle', targetAngle, {
            onChange: () => {
                this.canvas.renderAll()
            },
            duration: 200,
            onComplete: () => {
                fabricGroup.set({angle: targetAngle})
                fabricGroup.setCoords()

                for (const piece of snapGroup.pieces) {
                    piece.rotation = (piece.rotation + angleDelta + 360) % 360
                }
                this.canvas.renderAll()
                this.rotating = false
            }
        })
    }

    private trySnapToNeighbor(draggedGroup: SnapGroupDisplay) {
        for (const piece of draggedGroup.pieces) {
            const bodyCenter = piece.getBodyCenter()
            const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[piece.rotation]

            for (const {colOffset, rowOffset} of NEIGHBOR_OFFSETS) {
                const neighborKey = `${piece.col + colOffset}:${piece.row + rowOffset}`
                const neighbor = this.piecesByLocation.get(neighborKey)
                if (!neighbor || draggedGroup.pieces.has(neighbor)) continue

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

                    // Move the entire snap group by the correction
                    draggedGroup.fabricGroup.set({
                        left: (draggedGroup.fabricGroup.left ?? 0) + correctionX,
                        top: (draggedGroup.fabricGroup.top ?? 0) + correctionY,
                    })
                    draggedGroup.fabricGroup.setCoords()

                    // Merge snap groups and update visuals
                    const merged = draggedGroup.merge(neighbor.snapGroup, this.canvas)
                    merged.updateStrokeOpacity(this.piecesByLocation)
                    this.canvas.renderAll()
                    return
                }
            }
        }
    }
}
