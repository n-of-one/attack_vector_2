import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {JigsawEnterData} from "../JigsawServerActionProcessor";
import {generatePieceConfigs} from "../component/JigsawShapes";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";

class JigsawCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    imageLoaded_: boolean = false
    piecesByLocation: { [key: string]: JigsawPieceDisplay } = {}

    imageLoaded() {
        this.imageLoaded_ = true
    }

    init(data: JigsawEnterData, dispatch: Dispatch, store: Store) {
        if (!this.imageLoaded_) {
            setTimeout(() => this.init(data, dispatch, store), 100)
            return
        }
        this.dispatch = dispatch
        this.store = store

        const canvasWidth = 900
        const canvasHeight = 700

        this.canvas = new fabric.Canvas('jigsawCanvas', {
            width: canvasWidth,
            height: canvasHeight,
            backgroundColor: "#1a1a2e",
            selection: false,
        })

        const puzzleCols = data.gridSize
        const puzzleRows = data.gridSize

        // Calculate piece size to fit within canvas with some margin
        const maxPieceSize = Math.min(
            (canvasWidth - 80) / (puzzleCols + 1),
            (canvasHeight - 80) / (puzzleRows + 1)
        )
        const pieceSize = Math.min(maxPieceSize, 150)

        const sourceImg = document.getElementById('jigsawSourceImage') as HTMLImageElement
        const pieces = generatePieceConfigs(puzzleCols, puzzleRows)

        this.piecesByLocation = {}
        for (const config of pieces) {
            const display = new JigsawPieceDisplay(
                this.canvas, config.col, config.row, config, sourceImg,
                puzzleCols, puzzleRows, pieceSize, canvasWidth, canvasHeight
            )
            this.piecesByLocation[`${config.col}:${config.row}`] = display
        }

        this.canvas.renderAll()
    }
}

export const jigsawCanvas = new JigsawCanvas()
