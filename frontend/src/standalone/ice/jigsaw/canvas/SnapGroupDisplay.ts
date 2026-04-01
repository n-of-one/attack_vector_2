import {fabric} from "fabric"
import {Canvas} from "fabric/fabric-impl"
import {JigsawPieceDisplay} from "./JigsawPieceDisplay"

const SNAP_TOLERANCE_PIXELS = 15

const NEIGHBOR_OFFSETS: ReadonlyArray<{ colOffset: number, rowOffset: number }> = [
    {colOffset: -1, rowOffset: 0},
    {colOffset: 1, rowOffset: 0},
    {colOffset: 0, rowOffset: -1},
    {colOffset: 0, rowOffset: 1},
]

const FABRIC_GROUP_OPTIONS = {
    selectable: true,
    hasControls: false,
    hasBorders: false,
    lockRotation: true,
    hoverCursor: 'grab',
    subTargetCheck: false,
    perPixelTargetFind: true,
}

export class SnapGroupDisplay {

    readonly pieces: Set<JigsawPieceDisplay>
    readonly fabricGroup: fabric.Group
    readonly canvas: Canvas

    constructor(pieces: Set<JigsawPieceDisplay>, canvas: Canvas) {
        this.pieces = pieces
        this.canvas = canvas
        const displayObjects = [...pieces].map(p => p.displayObject)
        this.fabricGroup = new fabric.Group(displayObjects, FABRIC_GROUP_OPTIONS)
        this.fabricGroup.data = this
        for (const piece of pieces) {
            piece.snapGroup = this
        }
        canvas.add(this.fabricGroup)
    }

    /** Animate a 90-degree rotation around the group's bounding box center. */
    rotate(clockwise: boolean, onComplete: () => void) {
        const angleDelta = clockwise ? 90 : -90
        const startAngle = this.fabricGroup.angle ?? 0
        const targetAngle = startAngle + angleDelta

        this.fabricGroup.animate('angle', targetAngle, {
            onChange: () => this.canvas.renderAll(),
            duration: 200,
            onComplete: () => {
                this.fabricGroup.set({angle: targetAngle})
                this.fabricGroup.setCoords()
                for (const piece of this.pieces) {
                    piece.rotation = (piece.rotation + angleDelta + 360) % 360
                }
                this.canvas.renderAll()
                onComplete()
            }
        })
    }

    /** Check all pieces for a snappable neighbor. If found, snap and merge groups. Returns true if snapped. */
    trySnap(piecesByLocation: Map<string, JigsawPieceDisplay>) {
        for (const piece of this.pieces) {
            for (const {colOffset, rowOffset} of NEIGHBOR_OFFSETS) {
                const neighbor = piecesByLocation.get(`${piece.col + colOffset}:${piece.row + rowOffset}`)
                if (!neighbor || this.pieces.has(neighbor)) continue

                const correction = piece.checkSnapTo(neighbor, SNAP_TOLERANCE_PIXELS)
                if (!correction) continue

                // Align this group with the neighbor
                this.fabricGroup.set({
                    left: (this.fabricGroup.left ?? 0) + correction.x,
                    top: (this.fabricGroup.top ?? 0) + correction.y,
                })
                this.fabricGroup.setCoords()

                // Merge and update visuals
                const merged = this.merge(neighbor.snapGroup, this.canvas)
                merged.updateStrokeOpacity(piecesByLocation)
                this.canvas.renderAll()
            }
        }
    }

    /** Merge this snap group with another. Returns the new combined SnapGroupDisplay. */
    merge(other: SnapGroupDisplay, canvas: Canvas): SnapGroupDisplay {
        // Record absolute body centers before destruction
        const positions = new Map<JigsawPieceDisplay, { x: number, y: number }>()
        for (const piece of this.pieces) positions.set(piece, piece.getBodyCenter())
        for (const piece of other.pieces) positions.set(piece, piece.getBodyCenter())

        // Remove fabric groups from canvas
        canvas.remove(this.fabricGroup)
        if (other.fabricGroup !== this.fabricGroup) {
            canvas.remove(other.fabricGroup)
        }

        // Extract displayObjects from old groups
        for (const piece of this.pieces) this.fabricGroup.remove(piece.displayObject)
        for (const piece of other.pieces) other.fabricGroup.remove(piece.displayObject)

        // Restore absolute positions on extracted displayObjects
        const allPieces = new Set([...this.pieces, ...other.pieces])
        for (const piece of allPieces) {
            piece.restoreAbsolutePosition(positions.get(piece)!)
        }

        // Create merged group
        return new SnapGroupDisplay(allPieces, canvas)
    }

    /** Update stroke opacity for all pieces based on snapped neighbor count within this group. */
    updateStrokeOpacity(piecesByLocation: Map<string, JigsawPieceDisplay>) {
        for (const piece of this.pieces) {
            const snappedNeighborCount = NEIGHBOR_OFFSETS.filter(({colOffset, rowOffset}) => {
                const neighbor = piecesByLocation.get(`${piece.col + colOffset}:${piece.row + rowOffset}`)
                return neighbor && this.pieces.has(neighbor)
            }).length
            piece.updateSnappedNeighborCount(snappedNeighborCount)
        }
    }
}
