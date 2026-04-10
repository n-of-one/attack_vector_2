import {Container} from "pixi.js"
import {JigsawPieceDisplay} from "./JigsawPieceDisplay"
import type {JigsawCanvas} from "./JigsawCanvas"

const SNAP_TOLERANCE_PIXELS = 15

const NEIGHBOR_OFFSETS: ReadonlyArray<{ colOffset: number, rowOffset: number }> = [
    {colOffset: -1, rowOffset: 0},
    {colOffset: 1, rowOffset: 0},
    {colOffset: 0, rowOffset: -1},
    {colOffset: 0, rowOffset: 1},
]

export class SnapGroupDisplay {

    readonly pieces: Set<JigsawPieceDisplay>
    readonly container: Container
    private readonly canvas: JigsawCanvas

    constructor(pieces: Set<JigsawPieceDisplay>, canvas: JigsawCanvas) {
        this.pieces = pieces
        this.canvas = canvas
        this.container = new Container()
        this.container.eventMode = 'static'
        ;(this.container as any).__snapGroup = this
        for (const piece of pieces) {
            this.container.addChild(piece.container)
            piece.snapGroup = this
        }
        canvas.addSnapGroup(this)
        this.recomputePivot()
    }

    /** Set pivot to the centroid of piece body centers so rotation pivots around the group center. */
    private recomputePivot() {
        // Before this call, the container is at position (0,0), pivot (0,0), rotation 0.
        // getBodyCenter for each piece returns the absolute world body center.
        let sx = 0, sy = 0
        for (const piece of this.pieces) {
            const c = piece.getBodyCenter()
            sx += c.x
            sy += c.y
        }
        const n = this.pieces.size
        const cx = sx / n
        const cy = sy / n
        // Setting pivot shifts children visually by -pivot; compensate by setting position = pivot.
        this.container.pivot.set(cx, cy)
        this.container.position.set(cx, cy)
    }

    /** Animate a 90-degree rotation around the group's pivot. */
    rotate(clockwise: boolean, onComplete: () => void) {
        const angleDeltaDeg = clockwise ? 90 : -90
        const angleDeltaRad = (angleDeltaDeg * Math.PI) / 180
        const startAngle = this.container.rotation
        const targetAngle = startAngle + angleDeltaRad
        const duration = 200
        const startTime = performance.now()

        const ticker = this.canvas.ticker
        const tick = () => {
            const elapsed = performance.now() - startTime
            if (elapsed >= duration) {
                this.container.rotation = targetAngle
                ticker.remove(tick)
                for (const piece of this.pieces) {
                    piece.rotation = (piece.rotation + angleDeltaDeg + 360) % 360
                }
                onComplete()
                return
            }
            this.container.rotation = startAngle + angleDeltaRad * (elapsed / duration)
        }
        ticker.add(tick)
    }

    /** Check all pieces for a snappable neighbor. If found, snap and merge groups. */
    trySnap(piecesByLocation: Map<string, JigsawPieceDisplay>) {
        let bestDistance = SNAP_TOLERANCE_PIXELS
        let bestCorrection: { x: number, y: number } | null = null
        let bestNeighbor: JigsawPieceDisplay | null = null

        for (const piece of this.pieces) {
            for (const {colOffset, rowOffset} of NEIGHBOR_OFFSETS) {
                const neighbor = piecesByLocation.get(`${piece.col + colOffset}:${piece.row + rowOffset}`)
                if (!neighbor || this.pieces.has(neighbor)) continue

                const correction = piece.checkSnapTo(neighbor, bestDistance)
                if (!correction) continue

                const distance = Math.max(Math.abs(correction.x), Math.abs(correction.y))
                bestDistance = distance
                bestCorrection = correction
                bestNeighbor = neighbor
            }
        }

        if (!bestCorrection || !bestNeighbor) return

        // Apply snap correction to this group's world position.
        this.container.position.set(
            this.container.position.x + bestCorrection.x,
            this.container.position.y + bestCorrection.y,
        )

        const merged = this.merge(bestNeighbor.snapGroup)
        merged.updateStrokeOpacity(piecesByLocation)
    }

    /** Merge this snap group with another. Returns the new combined SnapGroupDisplay. */
    merge(other: SnapGroupDisplay): SnapGroupDisplay {
        // Record absolute world body centers AFTER snap correction has been applied.
        const positions = new Map<JigsawPieceDisplay, { x: number, y: number }>()
        for (const piece of this.pieces) positions.set(piece, piece.getBodyCenter())
        for (const piece of other.pieces) positions.set(piece, piece.getBodyCenter())

        // Remove old group containers from the scene.
        this.canvas.removeSnapGroup(this)
        if (other !== this) this.canvas.removeSnapGroup(other)

        // Detach piece containers from old group containers.
        for (const piece of this.pieces) this.container.removeChild(piece.container)
        for (const piece of other.pieces) other.container.removeChild(piece.container)

        const allPieces = new Set<JigsawPieceDisplay>([...this.pieces, ...other.pieces])

        // Reset each piece to its recorded absolute position (parent will be identity in new group).
        for (const piece of allPieces) {
            piece.restoreAbsolutePosition(positions.get(piece)!)
        }

        this.container.destroy({children: false})
        if (other !== this) other.container.destroy({children: false})

        return new SnapGroupDisplay(allPieces, this.canvas)
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
