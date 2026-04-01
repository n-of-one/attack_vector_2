import {fabric} from "fabric"
import {Canvas} from "fabric/fabric-impl"
import {JigsawPieceDisplay} from "./JigsawPieceDisplay"

export const NEIGHBOR_OFFSETS: ReadonlyArray<{ colOffset: number, rowOffset: number }> = [
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

    private constructor(pieces: Set<JigsawPieceDisplay>, fabricGroup: fabric.Group) {
        this.pieces = pieces
        this.fabricGroup = fabricGroup
        fabricGroup.data = this
        for (const piece of pieces) {
            piece.snapGroup = this
        }
    }

    /** Create a snap group wrapping a single piece whose displayObject is already positioned. */
    static createForPiece(piece: JigsawPieceDisplay, canvas: Canvas): SnapGroupDisplay {
        const fabricGroup = new fabric.Group([piece.displayObject], FABRIC_GROUP_OPTIONS)
        const snapGroup = new SnapGroupDisplay(new Set([piece]), fabricGroup)
        canvas.add(fabricGroup)
        return snapGroup
    }

    /** Create a snap group from multiple pieces whose displayObjects are already positioned. */
    static createFromPieces(pieces: Set<JigsawPieceDisplay>, canvas: Canvas): SnapGroupDisplay {
        const displayObjects = [...pieces].map(p => p.displayObject)
        const fabricGroup = new fabric.Group(displayObjects, FABRIC_GROUP_OPTIONS)
        const snapGroup = new SnapGroupDisplay(pieces, fabricGroup)
        canvas.add(fabricGroup)
        return snapGroup
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

        // Restore absolute positions on standalone displayObjects
        const allPieces = new Set([...this.pieces, ...other.pieces])
        for (const piece of allPieces) {
            const pos = positions.get(piece)!
            piece.displayObject.set({angle: piece.rotation})
            piece.setBodyCenter(pos.x, pos.y)
        }

        // Create merged group
        return SnapGroupDisplay.createFromPieces(allPieces, canvas)
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
