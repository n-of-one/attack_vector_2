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

    constructor(canvas: Canvas, col: number, row: number, config: PieceConfig,
                sourceImage: HTMLImageElement, puzzleCols: number, puzzleRows: number,
                pieceSize: number, canvasWidth: number, canvasHeight: number) {
        this.canvas = canvas
        this.col = col
        this.row = row
        this.config = config
        this.pieceSize = pieceSize

        const tabSize = pieceSize * 0.2

        const extensionLeft = this.extension(config.left, tabSize)
        const extensionTop = this.extension(config.top, tabSize)
        const extensionRight = this.extension(config.right, tabSize)
        const extensionBottom = this.extension(config.bottom, tabSize)

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
            stroke: '#88aacc',
            strokeWidth: 1.5,
            left: scatteredX,
            top: scatteredY,
            selectable: true,
            hasControls: false,
            hasBorders: true,
            borderColor: '#44ff44',
            lockRotation: true,
            data: this,
            hoverCursor: 'grab',
        })

        canvas.add(this.path)
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
