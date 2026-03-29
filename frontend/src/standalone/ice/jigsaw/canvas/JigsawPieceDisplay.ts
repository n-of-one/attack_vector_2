import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
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
    canvas: Canvas
    col: number
    row: number
    config: PieceConfig
    sourceImage: HTMLImageElement
    puzzleCols: number
    puzzleRows: number
    pieceSize: number
}

export class JigsawPieceDisplay {

    private readonly canvas: Canvas
    readonly col: number
    readonly row: number
    private readonly config: PieceConfig
    private readonly pieceSize: number
    private readonly strokeWidth: number

    /** The main piece shape (fill + fading stroke). Child of displayObject. */
    private readonly piecePath: fabric.Path

    /** The single fabric object added to the canvas — a Group wrapping piecePath + optional borderPath. */
    readonly displayObject: fabric.Group

    // Offset from the path's left/top to the piece body's top-left corner.
    // Needed because outward tabs extend the bounding box beyond the piece body.
    private readonly extensionLeft: number
    private readonly extensionTop: number

    // Offset from the bounding-box center to the body center in unrotated local coords.
    // When the piece is rotated, this offset rotates with it.
    private readonly bodyOffsetX: number
    private readonly bodyOffsetY: number

    // All pieces snapped together share the same Set. Starts with just this piece.
    group: Set<JigsawPieceDisplay>

    // Current rotation in degrees: 0, 90, 180, or 270. Pieces start at a random rotation.
    rotation: number

    // How many neighbors this piece can have (2 for corners, 3 for edges, 4 for center pieces)
    readonly maxNeighborCount: number

    constructor(options: JigsawPieceDisplayOptions) {
        const {canvas, col, row, config, sourceImage, puzzleCols, puzzleRows, pieceSize} = options

        this.canvas = canvas
        this.col = col
        this.row = row
        this.config = config
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

        // Build path at local origin (extensionLeft, extensionTop) so all coordinates are non-negative
        const pathString = buildPiecePath(this.extensionLeft, this.extensionTop, pieceSize, config)

        this.piecePath = new fabric.Path(pathString, {
            fill: new fabric.Pattern({
                source: patternCanvas as unknown as HTMLImageElement, // fabric.js accepts canvas elements at runtime
                repeat: 'no-repeat',
            }),
            stroke: '#88aacc',
            strokeWidth: this.strokeWidth,
        })

        // Build a separate non-fading outline for flat (puzzle border) edges
        const children: fabric.Object[] = [this.piecePath]
        const borderString = buildBorderPath(this.extensionLeft, this.extensionTop, pieceSize, config)
        if (borderString) {
            children.push(new fabric.Path(borderString, {
                fill: '',
                stroke: '#fff',
                strokeWidth: this.strokeWidth * 1.5,
            }))
        }

        // Wrap in a Group so both paths move/rotate/z-order as one unit
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
        })

        const borderCount = [col === 0, col === puzzleCols - 1, row === 0, row === puzzleRows - 1]
            .filter(Boolean).length
        this.maxNeighborCount = 4 - borderCount

        this.rotation = config.rotation
        this.displayObject.set({angle: this.rotation})
        this.group = new Set([this])

        canvas.add(this.displayObject)
    }

    /** Canvas position of the piece body's top-left corner (excluding tab extensions). */
    getBodyOrigin(): { x: number, y: number } {
        return {
            x: (this.displayObject.left ?? 0) + this.extensionLeft,
            y: (this.displayObject.top ?? 0) + this.extensionTop,
        }
    }

    /** Move this piece so its body origin is at the given canvas position. */
    setBodyOrigin(x: number, y: number) {
        this.displayObject.set({
            left: x - this.extensionLeft,
            top: y - this.extensionTop,
        })
        this.displayObject.setCoords()
    }

    /** Canvas position of the body center, accounting for the current rotation. */
    getBodyCenter(): { x: number, y: number } {
        const bbCenter = this.displayObject.getCenterPoint()
        const angle = (this.displayObject.angle ?? 0) * Math.PI / 180
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        return {
            x: bbCenter.x + this.bodyOffsetX * cos - this.bodyOffsetY * sin,
            y: bbCenter.y + this.bodyOffsetX * sin + this.bodyOffsetY * cos,
        }
    }

    /** Position this piece so its body center (rotation-aware) is at the given canvas position. */
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
    }

    /** Move this piece by a delta (used for group dragging). */
    moveBy(deltaX: number, deltaY: number) {
        this.displayObject.set({
            left: (this.displayObject.left ?? 0) + deltaX,
            top: (this.displayObject.top ?? 0) + deltaY,
        })
        this.displayObject.setCoords()
    }

    /**
     * Animate a 90° rotation around a pivot point (in body-center space).
     * For a single piece the pivot is its own body center; for a group it's the average body center.
     * Position and angle interpolate together so the piece arcs smoothly to its new location.
     */
    animateRotation(pivotX: number, pivotY: number, clockwise: boolean, renderCallback: () => void, onComplete?: () => void) {
        const angleDelta = clockwise ? 90 : -90
        const startAngle = this.displayObject.angle ?? 0
        const targetAngle = startAngle + angleDelta
        this.rotation = (this.rotation + angleDelta + 360) % 360

        const startBodyCenter = this.getBodyCenter()

        // Rotate body center 90° around pivot (in screen-coords: y-down)
        // CW: (dx, dy) → (-dy, dx). CCW: (dx, dy) → (dy, -dx).
        const dx = startBodyCenter.x - pivotX
        const dy = startBodyCenter.y - pivotY
        const targetBodyCenterX = clockwise ? pivotX - dy : pivotX + dy
        const targetBodyCenterY = clockwise ? pivotY + dx : pivotY - dx

        this.displayObject.animate('angle', targetAngle, {
            onChange: () => {
                // Derive progress from the angle so position follows the same easing curve
                const progress = ((this.displayObject.angle ?? startAngle) - startAngle) / angleDelta
                const currentBodyCenterX = startBodyCenter.x + (targetBodyCenterX - startBodyCenter.x) * progress
                const currentBodyCenterY = startBodyCenter.y + (targetBodyCenterY - startBodyCenter.y) * progress

                this.setBodyCenter(currentBodyCenterX, currentBodyCenterY)
                renderCallback()
            },
            duration: 200,
            onComplete: () => {
                this.displayObject.set({angle: this.rotation})
                this.setBodyCenter(targetBodyCenterX, targetBodyCenterY)
                renderCallback()
                if (onComplete) onComplete()
            }
        })
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
