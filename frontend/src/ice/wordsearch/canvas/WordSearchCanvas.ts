import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {WordSearchIndicatorDisplay} from "./WordSearchIndicatorDisplay";
import {LETTERS_SELECTED} from "../reducer/WordSearchStateReducer";
import {webSocketConnection} from "../../../common/WebSocketConnection";

const MARGIN_TOP = 5
const MARGIN_LEFT = 5

const CELL_SIZE = 40
const ROWS = 20
const COLUMNS = 20

// const CELL_SIZE = 30
// const SIZE_VERTICAL = 30
// const ROWS = 22
// const COLUMNS = 50

// const CELL_SIZE = 30
// const ROWS = 22
// const COLUMNS = 50
// const HALF_CELL = Math.floor(CELL_SIZE / 2)


// const CELL_SIZE = 25
// const SIZE_VERTICAL = 25
// const ROWS = 26
// const COLUMNS = 61

// const CELL_SIZE = 25
// const SIZE_VERTICAL = 25
// const ROWS = 26
// const COLUMNS = 26

interface Position {
    x: number,
    y: number
}

class WordSearchCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    iceId: string | null = null
    letterCount = 0

    previousLetterPosition: Position | null = null


    lettersSelected: string[] = []
    letterStartPosition: { x: number, y: number } | null = null

    indicatorVisual: WordSearchIndicatorDisplay | null = null

    init(iceId: string, puzzleData: any, dispatch: Dispatch, store: Store) {
        this.iceId = iceId
        this.dispatch = dispatch;
        this.store = store;

        const canvas = new fabric.Canvas('wordSearchCanvas', {
            width: 1552,
            height: 839,
            // height: 680,
            backgroundColor: "#333333",
        });
        this.canvas = canvas

        fabric.Object.prototype.originX = "center";
        fabric.Object.prototype.originY = 'center';

        setTimeout(function () {
            fabric.Image.fromURL("/img/frontier/ice/wordSearch/darknoise_4_1552_852-2.png", (img) => {
                img.set({width: canvas.width, height: canvas.height, originX: 'left', originY: 'top'});
                canvas.setBackgroundImage(img, canvas.renderAll.bind(canvas));
            });
        }, 100);

        canvas.selection = false;


        // horizontal lines
        for (let y = 0; y < ROWS + 1; y++) {
            this.drawLine(0, y, COLUMNS, y)
        }

        // vertical lines
        for (let x = 0; x < COLUMNS + 1; x++) {
            this.drawLine(x, 0, x, ROWS)
        }

        // for (let x = 0; x < 4; x++) {
        //     for (let y = 0; y < 4; y++) {
        //         this.drawLetter(x, y, this.randomLetter())
        //     }
        // }


        this.canvas.discardActiveObject();
        this.canvas.renderAll();
    }


    private drawLine(x1: number, y1: number, x2: number, y2: number) {
        const lineData = [
            MARGIN_LEFT + x1 * CELL_SIZE,
            MARGIN_TOP + y1 * CELL_SIZE,
            MARGIN_LEFT + x2 * CELL_SIZE,
            MARGIN_TOP + y2 * CELL_SIZE
        ]
        const line = new fabric.Line(
            lineData, {
                stroke: "#aaa",
                strokeWidth: 2,
                strokeDashArray: [2, 2],
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
        this.dispatch({type: LETTERS_SELECTED, payload: this.lettersSelected})
    }

    processLetters() {
        if (this.lettersSelected.length >= 2) {
            const payload = {iceId: this.iceId, letters: this.lettersSelected}
            webSocketConnection.send("/av/ice/wordSearch/selected", JSON.stringify(payload))
        }

        this.lettersSelected = []
        this.letterStartPosition = null
        this.updateLettersSelected()
    }


    private determineLetterPosition(x: number, y: number) {
        const letterX = Math.floor((x - MARGIN_LEFT) / CELL_SIZE)
        const letterY = Math.floor((y - MARGIN_TOP) / CELL_SIZE)
        return {x: letterX, y: letterY}
    }
}

export const wordSearchCanvas = new WordSearchCanvas()