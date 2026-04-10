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
    col: number
    row: number
    config: PieceConfig
    sharedTexture: Texture
    sourceWidth: number
    sourceHeight: number
    puzzleCols: number
    puzzleRows: number
    pieceWidth: number
    pieceHeight: number
}

export class JigsawPieceDisplay {

    readonly col: number
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
            col, row, config, sharedTexture, sourceWidth, sourceHeight,
            puzzleCols, puzzleRows, pieceWidth, pieceHeight
        } = options

        this.col = col
        this.row = row
        this.pieceWidth = pieceWidth
        this.pieceHeight = pieceHeight

        const vTabSize = tabSizeForEdge('left', pieceWidth, pieceHeight)
        const hTabSize = tabSizeForEdge('top', pieceWidth, pieceHeight)

        this.extensionLeft = this.extensionSize(config.left, vTabSize)
        this.extensionTop = this.extensionSize(config.top, hTabSize)
        const extensionRight = this.extensionSize(config.right, vTabSize)
        const extensionBottom = this.extensionSize(config.bottom, hTabSize)

        this.bodyOffsetX = (this.extensionLeft - extensionRight) / 2
        this.bodyOffsetY = (this.extensionTop - extensionBottom) / 2

        const bbWidth = this.extensionLeft + pieceWidth + extensionRight
        const bbHeight = this.extensionTop + pieceHeight + extensionBottom
        const bbCenterLocalX = bbWidth / 2
        const bbCenterLocalY = bbHeight / 2

        // Piece outline as polygon points (local coords, origin at top-left of bounding box).
        const polygonPoints = buildPiecePoints(
            this.extensionLeft, this.extensionTop, pieceWidth, pieceHeight, config)

        // Mask: filled outline, clips the textured sprite to the piece shape.
        const mask = new Graphics().poly(polygonPoints).fill(0xffffff)

        // Sprite: shared source texture, positioned/scaled so the correct image region
        // appears within this piece's bounding box.
        const cropOffsetX = (sourceWidth - IMAGE_WIDTH) / 2
        const cropOffsetY = (sourceHeight - IMAGE_HEIGHT) / 2
        const sourceX = cropOffsetX + (col * pieceWidth - this.extensionLeft) / PUZZLE_SCALE
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
            for (const seg of borderSegments) {
                borderGraphics.moveTo(seg[0], seg[1]).lineTo(seg[2], seg[3])
            }
            borderGraphics.stroke({color: BORDER_COLOR, width: BORDER_WIDTH, alpha: 1})
            this.container.addChild(borderGraphics)
        }

        // Pivot on the bounding box center so rotation matches fabric's center-origin behavior.
        this.container.pivot.set(bbCenterLocalX, bbCenterLocalY)
        this.container.position.set(config.x, config.y)

        // Hit area for per-pixel-like click detection (polygon in local pre-pivot coords).
        this.container.hitArea = new Polygon(polygonPoints)
        this.container.eventMode = 'static'
        this.container.cursor = 'grab'
        ;(this.container as any).__piece = this

        const borderCount = [col === 0, col === puzzleCols - 1, row === 0, row === puzzleRows - 1]
            .filter(Boolean).length
        this.maxNeighborCount = 4 - borderCount

        this.rotation = config.rotation
        this.container.rotation = (this.rotation * Math.PI) / 180
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
        const gAngle = parent.rotation as number
        const gCos = Math.cos(gAngle)
        const gSin = Math.sin(gAngle)
        const dx = inParentX - parent.pivot.x
        const dy = inParentY - parent.pivot.y
        return {
            x: parent.position.x + dx * gCos - dy * gSin,
            y: parent.position.y + dx * gSin + dy * gCos,
        }
    }

    /** Position this piece relative to an anchor piece, matching the anchor's rotation. */
    positionRelativeTo(anchor: JigsawPieceDisplay) {
        this.rotation = anchor.rotation
        this.container.rotation = (this.rotation * Math.PI) / 180

        const anchorCenter = anchor.getBodyCenter()
        const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[this.rotation]
        const colOffset = this.col - anchor.col
        const rowOffset = this.row - anchor.row

        this.setBodyCenter(
            anchorCenter.x + colOffset * this.pieceWidth * cos - rowOffset * this.pieceHeight * sin,
            anchorCenter.y + colOffset * this.pieceWidth * sin + rowOffset * this.pieceHeight * cos,
        )
    }

    /** Restore an absolute world position after being extracted from a snap group. */
    restoreAbsolutePosition(bodyCenter: { x: number, y: number }) {
        this.container.rotation = (this.rotation * Math.PI) / 180
        this.setBodyCenter(bodyCenter.x, bodyCenter.y)
    }

    /** Check if this piece is close enough to snap to the given neighbor. */
    checkSnapTo(neighbor: JigsawPieceDisplay, tolerance: number): { x: number, y: number } | null {
        if (neighbor.rotation !== this.rotation) return null

        const bodyCenter = this.getBodyCenter()
        const neighborCenter = neighbor.getBodyCenter()

        const colOffset = neighbor.col - this.col
        const rowOffset = neighbor.row - this.row
        const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[this.rotation]

        const expectedNeighborX = bodyCenter.x + colOffset * this.pieceWidth * cos - rowOffset * this.pieceHeight * sin
        const expectedNeighborY = bodyCenter.y + colOffset * this.pieceWidth * sin + rowOffset * this.pieceHeight * cos

        const dx = neighborCenter.x - expectedNeighborX
        const dy = neighborCenter.y - expectedNeighborY

        if (Math.abs(dx) >= tolerance || Math.abs(dy) >= tolerance) return null

        return {x: dx, y: dy}
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
        return edge.dir === 'out' ? tabSize : 0
    }

    /** Compute line segments for the white border on flat (outer) edges. */
    private collectBorderSegments(config: PieceConfig): Array<[number, number, number, number]> {
        const segments: Array<[number, number, number, number]> = []
        const oX = this.extensionLeft
        const oY = this.extensionTop
        const pw = this.pieceWidth
        const ph = this.pieceHeight
        if (config.top.dir === 'flat') segments.push([oX, oY, oX + pw, oY])
        if (config.right.dir === 'flat') segments.push([oX + pw, oY, oX + pw, oY + ph])
        if (config.bottom.dir === 'flat') segments.push([oX + pw, oY + ph, oX, oY + ph])
        if (config.left.dir === 'flat') segments.push([oX, oY + ph, oX, oY])
        return segments
    }
}
