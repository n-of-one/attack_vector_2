import {Container} from "pixi.js"
import {JigsawPieceDisplay} from "./JigsawPieceDisplay"
import type {JigsawCanvas} from "./JigsawCanvas"

const NEIGHBOR_OFFSETS: ReadonlyArray<{ columnOffset: number, rowOffset: number }> = [
    {columnOffset: -1, rowOffset: 0},
    {columnOffset: 1, rowOffset: 0},
    {columnOffset: 0, rowOffset: -1},
    {columnOffset: 0, rowOffset: 1},
]

export class SnapGroupDisplay {

    readonly id: string
    readonly pieces: Set<JigsawPieceDisplay>
    readonly container: Container
    private readonly canvas: JigsawCanvas
    private activeRotation: (() => void) | null = null

    constructor(id: string, pieces: Set<JigsawPieceDisplay>, canvas: JigsawCanvas) {
        this.id = id
        this.pieces = pieces
        this.canvas = canvas
        this.container = new Container()
        this.container.eventMode = 'static'
        ;(this.container as any).__snapGroup = this
        ;(this.container as any).__groupId = id
        for (const piece of pieces) {
            this.container.addChild(piece.container)
            piece.snapGroup = this
        }
        canvas.addSnapGroup(this)
        this.recomputePivot()
    }

    /** Set pivot to the centroid of piece body centers so rotation pivots around the group center. */
    recomputePivot() {
        // Before this call, the container is at position (0,0), pivot (0,0), rotation 0.
        // getBodyCenter for each piece returns the absolute world body center.
        let sumX = 0, sumY = 0
        for (const piece of this.pieces) {
            const center = piece.getBodyCenter()
            sumX += center.x
            sumY += center.y
        }
        const count = this.pieces.size
        const centerX = sumX / count
        const centerY = sumY / count
        // Setting pivot shifts children visually by -pivot; compensate by setting position = pivot.
        this.container.pivot.set(centerX, centerY)
        this.container.position.set(centerX, centerY)
    }

    /** Animate a 90-degree rotation around the group's pivot. */
    rotate(clockwise: boolean, onComplete: () => void) {
        const angleDeltaDegrees = clockwise ? 90 : -90
        const angleDeltaRadians = (angleDeltaDegrees * Math.PI) / 180
        const startAngle = this.container.rotation
        const targetAngle = startAngle + angleDeltaRadians
        const duration = 200
        const startTime = performance.now()

        const ticker = this.canvas.ticker
        const tick = () => {
            const elapsed = performance.now() - startTime
            if (elapsed >= duration) {
                this.container.rotation = targetAngle
                ticker.remove(tick)
                this.activeRotation = null
                for (const piece of this.pieces) {
                    piece.rotation = (piece.rotation + angleDeltaDegrees + 360) % 360
                }
                onComplete()
                return
            }
            this.container.rotation = startAngle + angleDeltaRadians * (elapsed / duration)
        }
        this.activeRotation = () => ticker.remove(tick)
        ticker.add(tick)
    }

    /** Stop any in-flight rotation tween without firing onComplete. Used when a server SNAP
     *  supersedes a local rotation: the old container is about to be destroyed and the new
     *  merged group will carry the server's authoritative rotation. */
    cancelRotation() {
        if (!this.activeRotation) return
        this.activeRotation()
        this.activeRotation = null
    }

    /** Merge this snap group with another. Returns the new combined SnapGroupDisplay. */
    merge(other: SnapGroupDisplay): SnapGroupDisplay {
        // Record absolute world body centers.
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

        return new SnapGroupDisplay(this.id, allPieces, this.canvas)
    }

    /** Update stroke opacity for all pieces based on snapped neighbor count within this group. */
    updateStrokeOpacity(piecesByLocation: Map<string, JigsawPieceDisplay>) {
        for (const piece of this.pieces) {
            const snappedNeighborCount = NEIGHBOR_OFFSETS.filter(({columnOffset, rowOffset}) => {
                const neighbor = piecesByLocation.get(`${piece.column + columnOffset}:${piece.row + rowOffset}`)
                return neighbor && this.pieces.has(neighbor)
            }).length
            piece.updateSnappedNeighborCount(snappedNeighborCount)
        }
    }
}
