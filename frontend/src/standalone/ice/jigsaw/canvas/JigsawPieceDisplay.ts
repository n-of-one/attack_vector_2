import {fabric} from "fabric";
import {buildBorderPath, buildPiecePath, EdgeConfig, PieceConfig} from "../component/JigsawShapes";

const TAB_SIZE_RATIO = 0.08

/** Opacity steps indexed by max possible neighbors:
 *  Corner (2 max):  0 → 100%, 1 → 60%, 2 → 0%
 *  Edge (3 max):    0 → 100%, 1 → 60%, 2 → 40%, 3 → 0%
 *  Center (4 max):  0 → 100%, 1 → 60%, 2 → 40%, 3 → 30%, 4 → 0% */
const OPACITY_STEPS: { [maxNeighbors: number]: number[] } = {
    2: [1.0, 0.6, 0],
    3: [1.0, 0.6, 0.4, 0],
    4: [1.0, 0.6, 0.4, 0.3, 0],
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

    /** Group wrapping piecePath + optional borderPath. */
    readonly displayObject: fabric.Group

    private readonly extensionLeft: number
    private readonly extensionTop: number

    // Offset from the bounding-box center to the body center in unrotated local coords.
    private readonly bodyOffsetX: number
    private readonly bodyOffsetY: number

    // All pieces snapped together share the same Set. Starts with just this piece.
    group: Set<JigsawPieceDisplay>

    // Current rotation in degrees: 0, 90, 180, or 270.
    rotation: number

    // How many neighbors this piece can have (2 for corners, 3 for edges, 4 for center pieces)
    readonly maxNeighborCount: number

    /** When this piece is part of a snap fabric group, references that group.
     *  null when the piece's displayObject is directly on the canvas. */
    snapFabricGroup: fabric.Group | null = null

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
            selectable: true,
            hasControls: false,
            hasBorders: false,
            lockRotation: true,
            data: this,
            hoverCursor: 'grab',
            subTargetCheck: false,
            perPixelTargetFind: true,
        })

        const borderCount = [col === 0, col === puzzleCols - 1, row === 0, row === puzzleRows - 1]
            .filter(Boolean).length
        this.maxNeighborCount = 4 - borderCount

        this.rotation = config.rotation
        this.displayObject.set({angle: this.rotation})
        this.group = new Set([this])
    }

    /** The fabric object currently on the canvas: snap group if grouped, displayObject if standalone. */
    getCanvasObject(): fabric.Object {
        return this.snapFabricGroup ?? this.displayObject
    }

    /** Canvas position of the body center. Works whether the piece is standalone or inside a snap group,
     *  because calcTransformMatrix accounts for all parent group transforms. */
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

    /** Position this piece so its body center is at the given canvas position.
     *  Only valid when the piece is standalone (not inside a snap fabric group). */
    setBodyCenter(x: number, y: number) {
        const angle = (this.displayObject.angle ?? 0) * Math.PI / 180
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        const bbCenterX = x - (this.bodyOffsetX * cos - this.bodyOffsetY * sin)
        const bbCenterY = y - (this.bodyOffsetX * sin + this.bodyOffsetY * cos)
        this.displayObject.setPositionByOrigin(
            new fabric.Point(bbCenterX, bbCenterY),
            'center', 'center'
        )
        this.displayObject.setCoords()
    }

    /** Update the stroke opacity based on how many neighbors are snapped to this piece. */
    updateSnappedNeighborCount(snappedNeighborCount: number) {
        const opacity = OPACITY_STEPS[this.maxNeighborCount][snappedNeighborCount]
        this.piecePath.set({stroke: `rgba(136, 170, 204, ${opacity})`})
        this.displayObject.set({dirty: true})
        if (this.snapFabricGroup) {
            this.snapFabricGroup.set({dirty: true})
        }
    }

    /** Merge this piece's group with another piece's group. All pieces end up sharing one Set. */
    mergeGroup(other: JigsawPieceDisplay) {
        if (this.group === other.group) return

        const mergedGroup = this.group
        for (const piece of other.group) {
            mergedGroup.add(piece)
            piece.group = mergedGroup
        }
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
