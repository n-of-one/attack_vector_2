import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {buildPiecePath, EdgeConfig, PieceConfig, ShapedEdge} from "../component/JigsawShapes";

export class JigsawPieceDisplay {

    canvas: Canvas
    col: number
    row: number
    config: PieceConfig
    pieceSize: number
    path: fabric.Path

    // Offset from the path's left/top to the piece body's top-left corner.
    // Needed because outward tabs extend the bounding box beyond the piece body.
    extensionLeft: number
    extensionTop: number

    // Offset from the bounding-box center to the body center in unrotated local coords.
    // When the piece is rotated, this offset rotates with it.
    bodyOffsetX: number
    bodyOffsetY: number

    // All pieces snapped together share the same Set. Starts with just this piece.
    group: Set<JigsawPieceDisplay>

    // Current rotation in degrees: 0, 90, 180, or 270. Pieces start at a random rotation.
    rotation: number

    constructor(canvas: Canvas, col: number, row: number, config: PieceConfig,
                sourceImage: HTMLImageElement, puzzleCols: number, puzzleRows: number,
                pieceSize: number, canvasWidth: number, canvasHeight: number) {
        this.canvas = canvas
        this.col = col
        this.row = row
        this.config = config
        this.pieceSize = pieceSize

        const tabSize = pieceSize * 0.2

        this.extensionLeft = this.extension(config.left, tabSize)
        this.extensionTop = this.extension(config.top, tabSize)
        const extensionRight = this.extension(config.right, tabSize)
        const extensionBottom = this.extension(config.bottom, tabSize)

        const extensionLeft = this.extensionLeft
        const extensionTop = this.extensionTop

        this.bodyOffsetX = (extensionLeft - extensionRight) / 2
        this.bodyOffsetY = (extensionTop - extensionBottom) / 2

        const patternCanvas = this.createPatternCanvas(sourceImage, col, row, puzzleCols, puzzleRows, pieceSize, tabSize,
            extensionLeft, extensionTop, extensionRight, extensionBottom)

        // Build path at local origin (extensionLeft, extensionTop) so all coordinates are non-negative
        const pathString = buildPiecePath(extensionLeft, extensionTop, pieceSize, config)

        // Scatter piece to random position on canvas
        const margin = tabSize * 2
        const scatteredX = margin + Math.random() * (canvasWidth - pieceSize - margin * 2)
        const scatteredY = margin + Math.random() * (canvasHeight - pieceSize - margin * 2)

        this.path = new fabric.Path(pathString, {
            fill: new fabric.Pattern({
                source: patternCanvas as unknown as HTMLImageElement, // fabric.js accepts canvas elements at runtime
                repeat: 'no-repeat',
            }),
            stroke: '#88aacc00',
            strokeWidth: 1.5,
            left: scatteredX,
            top: scatteredY,
            selectable: true,
            hasControls: false,
            hasBorders: false,
            lockRotation: true,
            data: this,
            hoverCursor: 'grab',
        })

        this.rotation = [0, 90, 180, 270][Math.floor(Math.random() * 4)]
        this.path.set({angle: this.rotation})
        this.group = new Set([this])

        canvas.add(this.path)
    }

    /** Canvas position of the piece body's top-left corner (excluding tab extensions). */
    getBodyOrigin(): { x: number, y: number } {
        return {
            x: (this.path.left ?? 0) + this.extensionLeft,
            y: (this.path.top ?? 0) + this.extensionTop,
        }
    }

    /** Move this piece so its body origin is at the given canvas position. */
    setBodyOrigin(x: number, y: number) {
        this.path.set({
            left: x - this.extensionLeft,
            top: y - this.extensionTop,
        })
        this.path.setCoords()
    }

    /** Canvas position of the body center, accounting for the current rotation. */
    getBodyCenter(): { x: number, y: number } {
        const bbCenter = this.path.getCenterPoint()
        const angle = (this.path.angle ?? 0) * Math.PI / 180
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        return {
            x: bbCenter.x + this.bodyOffsetX * cos - this.bodyOffsetY * sin,
            y: bbCenter.y + this.bodyOffsetX * sin + this.bodyOffsetY * cos,
        }
    }

    /** Position this piece so its body center (rotation-aware) is at the given canvas position. */
    setBodyCenter(x: number, y: number) {
        const angle = (this.path.angle ?? 0) * Math.PI / 180
        const cos = Math.cos(angle)
        const sin = Math.sin(angle)
        const bbCenterX = x - (this.bodyOffsetX * cos - this.bodyOffsetY * sin)
        const bbCenterY = y - (this.bodyOffsetX * sin + this.bodyOffsetY * cos)
        this.path.setPositionByOrigin(
            new fabric.Point(bbCenterX, bbCenterY),
            'center', 'center'
        )
        this.path.setCoords()
    }

    /** Move this piece by a delta (used for group dragging). */
    moveBy(deltaX: number, deltaY: number) {
        this.path.set({
            left: (this.path.left ?? 0) + deltaX,
            top: (this.path.top ?? 0) + deltaY,
        })
        this.path.setCoords()
    }

    /**
     * Animate a 90° rotation around a pivot point (in body-center space).
     * For a single piece the pivot is its own body center; for a group it's the average body center.
     * Position and angle interpolate together so the piece arcs smoothly to its new location.
     */
    animateRotation(pivotX: number, pivotY: number, clockwise: boolean, renderCallback: () => void, onComplete?: () => void) {
        const angleDelta = clockwise ? 90 : -90
        const startAngle = this.path.angle ?? 0
        const targetAngle = startAngle + angleDelta
        this.rotation = (this.rotation + angleDelta + 360) % 360

        const startBodyCenter = this.getBodyCenter()

        // Rotate body center 90° around pivot (in screen-coords: y-down)
        // CW: (dx, dy) → (-dy, dx). CCW: (dx, dy) → (dy, -dx).
        const dx = startBodyCenter.x - pivotX
        const dy = startBodyCenter.y - pivotY
        const targetBodyCenterX = clockwise ? pivotX - dy : pivotX + dy
        const targetBodyCenterY = clockwise ? pivotY + dx : pivotY - dx

        this.path.animate('angle', targetAngle, {
            onChange: () => {
                // Derive progress from the angle so position follows the same easing curve
                const progress = ((this.path.angle ?? startAngle) - startAngle) / angleDelta
                const currentBodyCenterX = startBodyCenter.x + (targetBodyCenterX - startBodyCenter.x) * progress
                const currentBodyCenterY = startBodyCenter.y + (targetBodyCenterY - startBodyCenter.y) * progress

                this.setBodyCenter(currentBodyCenterX, currentBodyCenterY)
                renderCallback()
            },
            duration: 200,
            onComplete: () => {
                this.path.set({angle: this.rotation})
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

    private extension(edge: EdgeConfig, tabSize: number): number {
        return (edge.dir !== 'flat' && (edge as ShapedEdge).dir === 'out') ? tabSize : 0
    }

    private createPatternCanvas(sourceImage: HTMLImageElement, col: number, row: number,
                                puzzleCols: number, puzzleRows: number, pieceSize: number, tabSize: number,
                                extensionLeft: number, extensionTop: number,
                                extensionRight: number, extensionBottom: number): HTMLCanvasElement {
        const boundingBoxWidth = extensionLeft + pieceSize + extensionRight
        const boundingBoxHeight = extensionTop + pieceSize + extensionBottom

        const puzzleWidth = pieceSize * puzzleCols
        const puzzleHeight = pieceSize * puzzleRows
        const imageScaleX = sourceImage.naturalWidth / puzzleWidth
        const imageScaleY = sourceImage.naturalHeight / puzzleHeight

        const sourceX = (col * pieceSize - extensionLeft) * imageScaleX
        const sourceY = (row * pieceSize - extensionTop) * imageScaleY
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
