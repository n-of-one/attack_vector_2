import {fabric} from "fabric";
import {Canvas} from "fabric/fabric-impl";
import {buildPiecePath, EdgeConfig, PieceConfig, ShapedEdge} from "../logic/JigsawLogic";

export class JigsawPieceDisplay {

    canvas: Canvas
    col: number
    row: number
    config: PieceConfig
    pieceSize: number
    path: fabric.Path

    constructor(canvas: Canvas, col: number, row: number, config: PieceConfig,
                sourceImg: HTMLImageElement, puzzleCols: number, puzzleRows: number,
                pieceSize: number, canvasWidth: number, canvasHeight: number) {
        this.canvas = canvas
        this.col = col
        this.row = row
        this.config = config
        this.pieceSize = pieceSize

        const tab = pieceSize * 0.2

        const extL = this.extension(config.left, tab)
        const extT = this.extension(config.top, tab)
        const extR = this.extension(config.right, tab)
        const extB = this.extension(config.bottom, tab)

        const patternCanvas = this.createPatternCanvas(sourceImg, col, row, puzzleCols, puzzleRows, pieceSize, tab, extL, extT, extR, extB)

        // Build path at local origin (extL, extT) so all coordinates are non-negative
        const pathStr = buildPiecePath(extL, extT, pieceSize, config)

        // Scatter piece to random position on canvas
        const margin = tab * 2
        const scatteredX = margin + Math.random() * (canvasWidth - pieceSize - margin * 2)
        const scatteredY = margin + Math.random() * (canvasHeight - pieceSize - margin * 2)

        this.path = new fabric.Path(pathStr, {
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

    private extension(edge: EdgeConfig, tab: number): number {
        return (edge.dir !== 'flat' && (edge as ShapedEdge).dir === 'out') ? tab : 0
    }

    private createPatternCanvas(sourceImg: HTMLImageElement, col: number, row: number,
                                puzzleCols: number, puzzleRows: number, size: number, tab: number,
                                extL: number, extT: number, extR: number, extB: number): HTMLCanvasElement {
        const bbW = extL + size + extR
        const bbH = extT + size + extB

        const puzzleW = size * puzzleCols
        const puzzleH = size * puzzleRows
        const imgScaleX = sourceImg.naturalWidth / puzzleW
        const imgScaleY = sourceImg.naturalHeight / puzzleH

        const sx = (col * size - extL) * imgScaleX
        const sy = (row * size - extT) * imgScaleY
        const sw = bbW * imgScaleX
        const sh = bbH * imgScaleY

        const pc = document.createElement('canvas')
        pc.width = bbW
        pc.height = bbH
        const ctx = pc.getContext('2d')!
        ctx.drawImage(sourceImg, sx, sy, sw, sh, 0, 0, bbW, bbH)
        return pc
    }
}
