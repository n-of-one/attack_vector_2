import {Container, Graphics, Polygon, Sprite, Texture} from "pixi.js"
import {buildPiecePoints, EdgeConfig, EdgeType, IMAGE_HEIGHT, IMAGE_WIDTH, PieceConfig, PUZZLE_SCALE,} from "../component/JigsawShapes";
import type {SnapGroupDisplay} from "./SnapGroupDisplay";

const TAB_SIZE_RATIO = 0.08

function tabSizeForEdge(edge: EdgeType, pieceWidth: number, pieceHeight: number): number {
    return ((edge === 'top' || edge === 'bottom') ? pieceWidth : pieceHeight) * TAB_SIZE_RATIO
}

/** Precomputed cos/sin for the four valid rotation angles (avoids floating-point drift). */
const PRECOMPUTED_ROTATION_TRIGONOMETRY: { [degrees: number]: { cos: number, sin: number } } = {
    0: {cos: 1, sin: 0},
    90: {cos: 0, sin: 1},
    180: {cos: -1, sin: 0},
    270: {cos: 0, sin: -1},
}

/** Opacity steps indexed by max possible neighbors. */
const OPACITY_STEPS: { [maxNeighbors: number]: number[] } = {
    2: [1.0, 0.6, 0.1],
    3: [1.0, 0.6, 0.4, 0.1],
    4: [1.0, 0.6, 0.4, 0.3, 0.1],
}

const STROKE_COLOR = 0x88aacc
const STROKE_WIDTH = 1
const BORDER_COLOR = 0xffffff
const BORDER_WIDTH = 2.5

export interface JigsawPieceDisplayOptions {
    column: number
    row: number
    config: PieceConfig
    sharedTexture: Texture
    sourceWidth: number
    sourceHeight: number
    puzzleColumns: number
    puzzleRows: number
    pieceWidth: number
    pieceHeight: number
}

export class JigsawPieceDisplay {

    readonly column: number
    readonly row: number
    private readonly pieceWidth: number
    private readonly pieceHeight: number

    /** Container for this piece. Child of a SnapGroupDisplay's container. */
    readonly container: Container

    private readonly strokeGraphics: Graphics

    private readonly extensionLeft: number
    private readonly extensionTop: number

    /** Offset from bounding-box center to body center, in unrotated local coords. */
    private readonly bodyOffsetX: number
    private readonly bodyOffsetY: number

    rotation: number

    readonly maxNeighborCount: number

    snapGroup!: SnapGroupDisplay

    constructor(options: JigsawPieceDisplayOptions) {
        const {
            column, row, config, sharedTexture, sourceWidth, sourceHeight,
            puzzleColumns, puzzleRows, pieceWidth, pieceHeight
        } = options

        this.column = column
        this.row = row
        this.pieceWidth = pieceWidth
        this.pieceHeight = pieceHeight

        const verticalTabSize = tabSizeForEdge('left', pieceWidth, pieceHeight)
        const horizontalTabSize = tabSizeForEdge('top', pieceWidth, pieceHeight)

        this.extensionLeft = this.extensionSize(config.left, verticalTabSize)
        this.extensionTop = this.extensionSize(config.top, horizontalTabSize)
        const extensionRight = this.extensionSize(config.right, verticalTabSize)
        const extensionBottom = this.extensionSize(config.bottom, horizontalTabSize)

        this.bodyOffsetX = (this.extensionLeft - extensionRight) / 2
        this.bodyOffsetY = (this.extensionTop - extensionBottom) / 2

        const boundingBoxWidth = this.extensionLeft + pieceWidth + extensionRight
        const boundingBoxHeight = this.extensionTop + pieceHeight + extensionBottom
        const boundingBoxCenterLocalX = boundingBoxWidth / 2
        const boundingBoxCenterLocalY = boundingBoxHeight / 2

        // Piece outline as polygon points (local coords, origin at top-left of bounding box).
        const polygonPoints = buildPiecePoints(
            this.extensionLeft, this.extensionTop, pieceWidth, pieceHeight, config)

        // Mask: filled outline, clips the textured sprite to the piece shape.
        const mask = new Graphics().poly(polygonPoints).fill(0xffffff)

        // Sprite: shared source texture, positioned/scaled so the correct image region
        // appears within this piece's bounding box.
        const cropOffsetX = (sourceWidth - IMAGE_WIDTH) / 2
        const cropOffsetY = (sourceHeight - IMAGE_HEIGHT) / 2
        const sourceX = cropOffsetX + (column * pieceWidth - this.extensionLeft) / PUZZLE_SCALE
        const sourceY = cropOffsetY + (row * pieceHeight - this.extensionTop) / PUZZLE_SCALE

        const sprite = new Sprite(sharedTexture)
        sprite.scale.set(PUZZLE_SCALE)
        sprite.position.set(-sourceX * PUZZLE_SCALE, -sourceY * PUZZLE_SCALE)
        sprite.mask = mask

        // Stroke overlay (variable opacity).
        this.strokeGraphics = new Graphics()
            .poly(polygonPoints)
            .stroke({color: STROKE_COLOR, width: STROKE_WIDTH, alpha: 1})

        this.container = new Container()
        this.container.addChild(mask)
        this.container.addChild(sprite)
        this.container.addChild(this.strokeGraphics)

        // Optional white border on flat (puzzle-edge) sides.
        const borderSegments = this.collectBorderSegments(config)
        if (borderSegments.length > 0) {
            const borderGraphics = new Graphics()
            for (const segment of borderSegments) {
                borderGraphics.moveTo(segment[0], segment[1]).lineTo(segment[2], segment[3])
            }
            borderGraphics.stroke({color: BORDER_COLOR, width: BORDER_WIDTH, alpha: 1})
            this.container.addChild(borderGraphics)
        }

        // Pivot on the bounding box center so rotation matches fabric's center-origin behavior.
        this.container.pivot.set(boundingBoxCenterLocalX, boundingBoxCenterLocalY)
        // Position/rotation come from the owning SnapGroupDisplay container; start at identity.
        this.container.position.set(0, 0)
        this.container.rotation = 0

        // Hit area for per-pixel-like click detection (polygon in local pre-pivot coords).
        this.container.hitArea = new Polygon(polygonPoints)
        this.container.eventMode = 'static'
        this.container.cursor = 'grab'
        ;(this.container as any).__piece = this

        const borderCount = [column === 0, column === puzzleColumns - 1, row === 0, row === puzzleRows - 1]
            .filter(Boolean).length
        this.maxNeighborCount = 4 - borderCount

        this.rotation = 0
    }

    /** World-space body center, computed directly from position/rotation/pivot.
     *  Does NOT rely on worldTransform (which Pixi only refreshes at render time). */
    getBodyCenter(): { x: number, y: number } {
        // In parent coords: bodyCenter = position + R(rotation) * bodyOffset.
        // (The pivot maps to position; body center is offset from pivot by bodyOffset.)
        const angle = this.container.rotation
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        const inParentX = this.container.position.x + this.bodyOffsetX * cos - this.bodyOffsetY * sin
        const inParentY = this.container.position.y + this.bodyOffsetX * sin + this.bodyOffsetY * cos

        // If this piece's parent is a snap group container, apply the group's transform.
        const parent = this.container.parent as any
        if (!parent || !parent.__snapGroup) {
            return {x: inParentX, y: inParentY}
        }
        const groupAngle = parent.rotation as number
        const groupCos = Math.cos(groupAngle)
        const groupSin = Math.sin(groupAngle)
        const deltaX = inParentX - parent.pivot.x
        const deltaY = inParentY - parent.pivot.y
        return {
            x: parent.position.x + deltaX * groupCos - deltaY * groupSin,
            y: parent.position.y + deltaX * groupSin + deltaY * groupCos,
        }
    }

    /** Position this piece relative to an anchor piece in un-rotated grid layout.
     *  The owning snap group container holds the rotation, so pieces are laid out flat
     *  here and the parent rotates them as a unit. */
    positionRelativeTo(anchor: JigsawPieceDisplay) {
        this.rotation = anchor.rotation
        const anchorCenter = anchor.getBodyCenter()
        const columnOffset = this.column - anchor.column
        const rowOffset = this.row - anchor.row

        this.setBodyCenter(
            anchorCenter.x + columnOffset * this.pieceWidth,
            anchorCenter.y + rowOffset * this.pieceHeight,
        )
    }

    /** Restore an absolute world position after being extracted from a snap group. */
    restoreAbsolutePosition(bodyCenter: { x: number, y: number }) {
        this.setBodyCenter(bodyCenter.x, bodyCenter.y)
    }

    /** Check if this piece is close enough to snap to the given neighbor. */
    checkSnapTo(neighbor: JigsawPieceDisplay, tolerance: number): { x: number, y: number } | null {
        if (neighbor.rotation !== this.rotation) return null

        const bodyCenter = this.getBodyCenter()
        const neighborCenter = neighbor.getBodyCenter()

        const columnOffset = neighbor.column - this.column
        const rowOffset = neighbor.row - this.row
        const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[this.rotation]

        const expectedNeighborX = bodyCenter.x + columnOffset * this.pieceWidth * cos - rowOffset * this.pieceHeight * sin
        const expectedNeighborY = bodyCenter.y + columnOffset * this.pieceWidth * sin + rowOffset * this.pieceHeight * cos

        const deltaX = neighborCenter.x - expectedNeighborX
        const deltaY = neighborCenter.y - expectedNeighborY

        if (Math.abs(deltaX) >= tolerance || Math.abs(deltaY) >= tolerance) return null

        return {x: deltaX, y: deltaY}
    }

    updateSnappedNeighborCount(snappedNeighborCount: number) {
        const opacity = OPACITY_STEPS[this.maxNeighborCount][snappedNeighborCount]
        this.strokeGraphics.alpha = opacity
    }

    /** Set container.position so this piece's body center lands at world (x, y).
     *  Only valid when the container has no parent or an identity-transform parent. */
    private setBodyCenter(x: number, y: number) {
        const angle = this.container.rotation
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        this.container.position.set(
            x - (this.bodyOffsetX * cos - this.bodyOffsetY * sin),
            y - (this.bodyOffsetX * sin + this.bodyOffsetY * cos),
        )
    }

    private extensionSize(edge: EdgeConfig, tabSize: number): number {
        return edge.direction === 'out' ? tabSize : 0
    }

    /** Compute line segments for the white border on flat (outer) edges. */
    private collectBorderSegments(config: PieceConfig): Array<[number, number, number, number]> {
        const segments: Array<[number, number, number, number]> = []
        const originX = this.extensionLeft
        const originY = this.extensionTop
        const width = this.pieceWidth
        const height = this.pieceHeight
        if (config.top.direction === 'flat') segments.push([originX, originY, originX + width, originY])
        if (config.right.direction === 'flat') segments.push([originX + width, originY, originX + width, originY + height])
        if (config.bottom.direction === 'flat') segments.push([originX + width, originY + height, originX, originY + height])
        if (config.left.direction === 'flat') segments.push([originX, originY + height, originX, originY])
        return segments
    }
}
