import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {WordSearchIndicatorDisplay} from "./WordSearchIndicatorDisplay";
import {LETTERS_SELECTED} from "../reducer/WordSearchStateReducer";
import {webSocketConnection} from "../../../../common/server/WebSocketConnection";
import {ServerEnterIceWordSearch} from "../WordSearchServerActionProcessor";
import {ice} from "../../../StandaloneGlobals";

const MARGIN_TOP = 5
const MARGIN_LEFT = 5
const MARGIN_BOTTOM = 5
const MARGIN_RIGHT = 5

interface Position {
    x: number,
    y: number
}

class WordSearchCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch

    cellSize: number = 0

    letterCount = 0
    previousLetterPosition: Position | null = null
    lettersSelected: string[] = []
    letterStartPosition: { x: number, y: number } | null = null

    indicatorVisual: WordSearchIndicatorDisplay | null = null

    init(puzzleData: ServerEnterIceWordSearch, dispatch: Dispatch, store: Store) {
        this.dispatch = dispatch;
        this.store = store;

        const rows = puzzleData.letterGrid.length
        const columns = puzzleData.letterGrid[0].length

        this.cellSize = (rows <= 20) ? 40: 30

        this.canvas = this.createCanvas(columns, rows)

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';


        this.canvas.selection = false;

        // horizontal lines
        for (let y = 0; y < rows + 1; y++) {
            this.drawLine(0, y, columns, y)
        }

        // vertical lines
        for (let x = 0; x < columns + 1; x++) {
            this.drawLine(x, 0, x, rows)
        }

        this.canvas.discardActiveObject();
        this.canvas.renderAll();
    }

    private createCanvas(columns: number, rows: number) : Canvas {
        const width = columns * this.cellSize + MARGIN_LEFT + MARGIN_RIGHT
        const height = rows * this.cellSize + MARGIN_TOP + MARGIN_BOTTOM

        const canvas = new fabric.Canvas('wordSearchCanvas', {
            width,
            height,
            backgroundColor: "#333333",
        });

        setTimeout(function () {
            fabric.Image.fromURL("/img/frontier/ice/wordSearch/darknoise_4_1552_852-2.png", (img) => {
                img.set({width: canvas.width, height: canvas.height, originX: 'left', originY: 'top'});
                canvas.setBackgroundImage(img, canvas.renderAll.bind(canvas));
            });
        }, 100);

        return canvas
    }


    private drawLine(x1: number, y1: number, x2: number, y2: number) {
        const lineData = [
            MARGIN_LEFT + x1 * this.cellSize,
            MARGIN_TOP + y1 * this.cellSize,
            MARGIN_LEFT + x2 * this.cellSize,
            MARGIN_TOP + y2 * this.cellSize
        ]
        const line = new fabric.Line(
            lineData, {
                stroke: "#000b",
                strokeWidth: 2,
                // strokeDashArray: [8, 2],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0.4
            });

        this.canvas.add(line)
    }


    mouseDown(event: MouseEvent) {
        if (this.indicatorVisual != null) {
            // we must have gone out of bounds. Process potential previous event first.
            this.mouseUp(event)
        }

        const pointer = this.canvas.getPointer(event);
        this.indicatorVisual = new WordSearchIndicatorDisplay(this.canvas, pointer.x, pointer.y)

        this.letterStartPosition = this.determineLetterPosition(pointer.x, pointer.y)
        const key = `${this.letterStartPosition.x}:${this.letterStartPosition.y}`
        this.lettersSelected = [key]
        this.updateLettersSelected()
    }

    mouseMove(event: MouseEvent) {
        if (this.indicatorVisual == null) return

        const pointer = this.canvas.getPointer(event);
        this.indicatorVisual.moved(pointer.x, pointer.y)
        const letterPosition = this.determineLetterPosition(pointer.x, pointer.y)

        const dx = this.letterStartPosition!!.x - letterPosition.x
        const dy = this.letterStartPosition!!.y - letterPosition.y

        if ((dx === 0 && dy === 0) ||
            (this.previousLetterPosition !== null && this.previousLetterPosition.x === letterPosition.x && this.previousLetterPosition.y === letterPosition.y)
        ) {
            return
        }
        this.previousLetterPosition = letterPosition

        this.lettersSelected = [`${this.letterStartPosition!!.x}:${this.letterStartPosition!!.y}`]
        if ((dx === 0 && dy !== 0) || (dx !== 0 && dy === 0) || (Math.abs(dx) === Math.abs(dy))) {
            let walk = 0
            const maxWalk = Math.max(Math.abs(dx), Math.abs(dy))
            while (walk < maxWalk) {
                walk++
                const x = this.letterStartPosition!!.x - Math.sign(dx) * walk
                const y = this.letterStartPosition!!.y - Math.sign(dy) * walk
                const key = `${x}:${y}`
                this.lettersSelected.push(key)
            }
        }
        this.updateLettersSelected()
    }

    mouseUp(event: MouseEvent) {
        if (this.indicatorVisual == null) return

        this.indicatorVisual.remove()
        this.indicatorVisual = null

        this.processLetters()
    }

    updateLettersSelected() {
        this.dispatch({type: LETTERS_SELECTED, selected: this.lettersSelected})
    }

    processLetters() {
        if (this.lettersSelected.length >= 2) {

            const toSend = this.lettersSelected
            // Delay the sending, so that the selected cells have some time to fade out.
            setTimeout(()=> {
                const payload = {iceId: ice.id, letters: toSend}
                webSocketConnection.send("/ice/wordSearch/selected", JSON.stringify(payload))
            },200)
        }

        this.lettersSelected = []
        this.letterStartPosition = null
        this.updateLettersSelected()
    }


    private determineLetterPosition(x: number, y: number) {
        const letterX = Math.floor((x - MARGIN_LEFT) / this.cellSize)
        const letterY = Math.floor((y - MARGIN_TOP) / this.cellSize)
        return {x: letterX, y: letterY}
    }
}

export const wordSearchCanvas = new WordSearchCanvas()