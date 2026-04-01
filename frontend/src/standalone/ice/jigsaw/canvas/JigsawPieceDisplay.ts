import {fabric} from "fabric";
import {buildBorderPath, buildPiecePath, EdgeConfig, PieceConfig} from "../component/JigsawShapes";
import type {SnapGroupDisplay} from "./SnapGroupDisplay";

const TAB_SIZE_RATIO = 0.08

/** Precomputed cos/sin for the four valid rotation angles (avoids floating-point drift). */
const PRECOMPUTED_ROTATION_TRIGONOMETRY: { [degrees: number]: { cos: number, sin: number } } = {
    0: {cos: 1, sin: 0},
    90: {cos: 0, sin: 1},
    180: {cos: -1, sin: 0},
    270: {cos: 0, sin: -1},
}

/** Opacity steps indexed by max possible neighbors:
 *  Corner (2 max):  0 → 100%, 1 → 60%, 2 → 0%
 *  Edge (3 max):    0 → 100%, 1 → 60%, 2 → 40%, 3 → 0%
 *  Center (4 max):  0 → 100%, 1 → 60%, 2 → 40%, 3 → 30%, 4 → 0% */
const OPACITY_STEPS: { [maxNeighbors: number]: number[] } = {
    2: [1.0, 0.6, 0.1],
    3: [1.0, 0.6, 0.4, 0.1],
    4: [1.0, 0.6, 0.4, 0.3, 0.1],
}

export interface JigsawPieceDisplayOptions {
    col: number
    row: number
    config: PieceConfig
    sourceImage: HTMLImageElement
    puzzleCols: number
    puzzleRows: number
    pieceSize: number
}

export class JigsawPieceDisplay {

    readonly col: number
    readonly row: number
    private readonly pieceSize: number
    private readonly strokeWidth: number

    /** The main piece shape (fill + fading stroke). Child of displayObject. */
    private readonly piecePath: fabric.Path

    /** Group wrapping piecePath + optional borderPath. Always a child of a SnapGroupDisplay's fabricGroup. */
    readonly displayObject: fabric.Group

    private readonly extensionLeft: number
    private readonly extensionTop: number

    // Offset from the bounding-box center to the body center in unrotated local coords.
    private readonly bodyOffsetX: number
    private readonly bodyOffsetY: number

    // Current rotation in degrees: 0, 90, 180, or 270.
    rotation: number

    // How many neighbors this piece can have (2 for corners, 3 for edges, 4 for center pieces)
    readonly maxNeighborCount: number

    /** The snap group this piece belongs to. Set during canvas initialization. */
    snapGroup!: SnapGroupDisplay

    constructor(options: JigsawPieceDisplayOptions) {
        const {col, row, config, sourceImage, puzzleCols, puzzleRows, pieceSize} = options

        this.col = col
        this.row = row
        this.pieceSize = pieceSize
        this.strokeWidth = 1

        const tabSize = pieceSize * TAB_SIZE_RATIO

        this.extensionLeft = this.extensionSize(config.left, tabSize)
        this.extensionTop = this.extensionSize(config.top, tabSize)
        const extensionRight = this.extensionSize(config.right, tabSize)
        const extensionBottom = this.extensionSize(config.bottom, tabSize)

        this.bodyOffsetX = (this.extensionLeft - extensionRight) / 2
        this.bodyOffsetY = (this.extensionTop - extensionBottom) / 2

        const patternCanvas = this.createPatternCanvas(sourceImage, puzzleCols, puzzleRows,
            extensionRight, extensionBottom)

        const pathString = buildPiecePath(this.extensionLeft, this.extensionTop, pieceSize, config)

        this.piecePath = new fabric.Path(pathString, {
            fill: new fabric.Pattern({
                source: patternCanvas as unknown as HTMLImageElement,
                repeat: 'no-repeat',
            }),
            stroke: '#88aacc',
            strokeWidth: this.strokeWidth,
        })

        const children: fabric.Object[] = [this.piecePath]
        const borderString = buildBorderPath(this.extensionLeft, this.extensionTop, pieceSize, config)
        if (borderString) {
            children.push(new fabric.Path(borderString, {
                fill: '',
                stroke: '#fff',
                strokeWidth: this.strokeWidth * 1.5,
            }))
        }

        this.displayObject = new fabric.Group(children, {
            left: config.x,
            top: config.y,
        })

        const borderCount = [col === 0, col === puzzleCols - 1, row === 0, row === puzzleRows - 1]
            .filter(Boolean).length
        this.maxNeighborCount = 4 - borderCount

        this.rotation = config.rotation
        this.displayObject.set({angle: this.rotation})
    }

    /** Canvas position of the body center (the piece square, ignoring tabs).
     *  Accounts for parent snap group transforms via calcTransformMatrix. */
    getBodyCenter(): { x: number, y: number } {
        const matrix = this.displayObject.calcTransformMatrix()
        const bbCenterX = matrix[4]
        const bbCenterY = matrix[5]
        const angle = Math.atan2(matrix[1], matrix[0])
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        return {
            x: bbCenterX + this.bodyOffsetX * cos - this.bodyOffsetY * sin,
            y: bbCenterY + this.bodyOffsetX * sin + this.bodyOffsetY * cos,
        }
    }

    /** Position this piece relative to an anchor piece, matching the anchor's rotation.
     *  Used before the piece is added to a snap group (during initial setup). */
    positionRelativeTo(anchor: JigsawPieceDisplay) {
        this.rotation = anchor.rotation
        this.displayObject.set({angle: this.rotation})

        const anchorCenter = anchor.getBodyCenter()
        const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[this.rotation]
        const colOffset = this.col - anchor.col
        const rowOffset = this.row - anchor.row

        this.setBodyCenter(
            anchorCenter.x + (colOffset * cos - rowOffset * sin) * this.pieceSize,
            anchorCenter.y + (colOffset * sin + rowOffset * cos) * this.pieceSize,
        )
    }

    /** Restore this piece's displayObject to an absolute position after being extracted from a snap group.
     *  Used during snap group merging. */
    restoreAbsolutePosition(bodyCenter: { x: number, y: number }) {
        this.displayObject.set({angle: this.rotation})
        this.setBodyCenter(bodyCenter.x, bodyCenter.y)
    }

    /** Check if this piece is close enough to snap to the given neighbor.
     *  Returns the correction to apply to this piece's snap group, or null if too far. */
    checkSnapTo(neighbor: JigsawPieceDisplay, tolerance: number): { x: number, y: number } | null {
        if (neighbor.rotation !== this.rotation) return null

        const bodyCenter = this.getBodyCenter()
        const neighborCenter = neighbor.getBodyCenter()

        const colOffset = neighbor.col - this.col
        const rowOffset = neighbor.row - this.row
        const {cos, sin} = PRECOMPUTED_ROTATION_TRIGONOMETRY[this.rotation]

        const expectedNeighborX = bodyCenter.x + (colOffset * cos - rowOffset * sin) * this.pieceSize
        const expectedNeighborY = bodyCenter.y + (colOffset * sin + rowOffset * cos) * this.pieceSize

        const dx = neighborCenter.x - expectedNeighborX
        const dy = neighborCenter.y - expectedNeighborY

        if (Math.abs(dx) >= tolerance || Math.abs(dy) >= tolerance) return null

        return {x: dx, y: dy}
    }

    /** Update the stroke opacity based on how many neighbors are snapped to this piece. */
    updateSnappedNeighborCount(snappedNeighborCount: number) {
        const opacity = OPACITY_STEPS[this.maxNeighborCount][snappedNeighborCount]
        this.piecePath.set({stroke: `rgba(136, 170, 204, ${opacity})`})
        this.displayObject.set({dirty: true})
        this.snapGroup?.fabricGroup.set({dirty: true})
    }

    /** Set the displayObject's position so the body center lands at (x, y).
     *  Only valid when the displayObject is not inside a fabric group. */
    private setBodyCenter(x: number, y: number) {
        const angle = (this.displayObject.angle ?? 0) * Math.PI / 180
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        const bbCenterX = x - (this.bodyOffsetX * cos - this.bodyOffsetY * sin)
        const bbCenterY = y - (this.bodyOffsetX * sin + this.bodyOffsetY * cos)
        this.displayObject.set({left: bbCenterX, top: bbCenterY})
        this.displayObject.setCoords()
    }

    private extensionSize(edge: EdgeConfig, tabSize: number): number {
        return edge.dir === 'out' ? tabSize : 0
    }

    private createPatternCanvas(sourceImage: HTMLImageElement,
                                puzzleCols: number, puzzleRows: number,
                                extensionRight: number, extensionBottom: number): HTMLCanvasElement {
        const boundingBoxWidth = this.extensionLeft + this.pieceSize + extensionRight
        const boundingBoxHeight = this.extensionTop + this.pieceSize + extensionBottom

        const puzzleWidth = this.pieceSize * puzzleCols
        const puzzleHeight = this.pieceSize * puzzleRows
        const imageScaleX = sourceImage.naturalWidth / puzzleWidth
        const imageScaleY = sourceImage.naturalHeight / puzzleHeight

        const sourceX = (this.col * this.pieceSize - this.extensionLeft) * imageScaleX
        const sourceY = (this.row * this.pieceSize - this.extensionTop) * imageScaleY
        const sourceWidth = boundingBoxWidth * imageScaleX
        const sourceHeight = boundingBoxHeight * imageScaleY

        const patternCanvas = document.createElement('canvas')
        patternCanvas.width = boundingBoxWidth
        patternCanvas.height = boundingBoxHeight
        const context = patternCanvas.getContext('2d')!
        context.drawImage(sourceImage, sourceX, sourceY, sourceWidth, sourceHeight, 0, 0, boundingBoxWidth, boundingBoxHeight)
        return patternCanvas
    }
}
