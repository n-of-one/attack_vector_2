import {Canvas} from "fabric/fabric-impl";
import {Dispatch, Store} from "redux";
import {fabric} from "fabric";
import {webSocketConnection} from "../../../common/WebSocketConnection";
import {SlowIceBlockDisplay} from "./SlowIceBlockDisplay";
import {hashCode} from "../../../common/Util";

export const BLOCK_WIDTH = 90
export const BLOCK_PADDING_LEFT = 10
export const BLOCK_HEIGHT = 40
export const BLOCK_PADDING_BOTTOM = 10

export const PADDING_LEFT = 20
export const PADDING_TOP = 20


interface SlowIceStats {
    totalUnits: number,
    unitsHacked: number
}

const deterministicShuffle = (list: number[], seed: number) => {
    const size = list.length
    const fibonacci = [1, 1]
    while (fibonacci.length < size) {
        const next = fibonacci[fibonacci.length - 1] + fibonacci[fibonacci.length - 2]
        fibonacci.push(next)
    }
    const indices = Array.from({length: size}, (_, i) => (i + fibonacci[(i + seed) % size]) % size)
    for (let i = 0; i < size; i++) {
        const j = indices[i];
        [list[i], list[j]] = [list[j], list[i]];
    }
}

class SlowIceCanvas {

    canvas: Canvas = null as unknown as Canvas
    store: Store = null as unknown as Store
    dispatch: Dispatch = null as unknown as Dispatch
    iceId: string | null = null

    totalUnits: number = 0
    unitsHacked: number = 0


    gridWidth = 0
    gridHeight = 0

    imagesLoaded = 0
    allImagesLoaded = false

    boxIndices = [] as number[]

    boxDisplaysByIndex = {} as {[index: number]: SlowIceBlockDisplay}


    imageLoaded(totalImages: number) {
        this.imagesLoaded++
        this.allImagesLoaded = (this.imagesLoaded === totalImages)
    }

    init(iceId: string, data: SlowIceStats, store: Store) {

        this.iceId = iceId
        this.store = store

        this.totalUnits = data.totalUnits
        this.unitsHacked = data.unitsHacked

        const sizeX = 10
        const sizeY = 10


        this.gridWidth = sizeX * (BLOCK_WIDTH + BLOCK_PADDING_LEFT)
        this.gridHeight = sizeY * (BLOCK_HEIGHT + BLOCK_PADDING_BOTTOM)

        const canvasWidth = this.gridWidth + PADDING_LEFT * 2
        const canvasHeight = this.gridHeight + PADDING_TOP * 2

        this.canvas = new fabric.Canvas('slowIceCanvas', {
            width: canvasWidth,
            height: canvasHeight,
            backgroundColor: "#222",
        });

        fabric.Object.prototype.originX = "left";
        fabric.Object.prototype.originY = 'top';

        this.canvas.selection = false;
        this.canvas.renderAll();


        setTimeout(() => {
            fabric.Image.fromURL("/img/tmp/cosmic-gb67dc5363_1920_PIRO4D_Pixabay_pixel.jpg", (img) => {
                img.set({width: canvasWidth, height: canvasHeight, originX: 'left', originY: 'top'});
                this.canvas.setBackgroundImage(img, this.canvas.renderAll.bind(this.canvas));
            });
        }, 100);
        this.initBoxIndices(iceId);

        this.renderBoxes()

        this.addHacker(15, 0)

        this.addHacker(15, 2.5)
        this.addHacker(15, 1)
        this.addHacker(15, 3.7)
        this.addHacker(15, .5)

    }

    addHacker(unitsPerSecond: number, delayS: number) {
        setTimeout(() => {
            setInterval(() => {
                this.unitsHacked += 5 * unitsPerSecond
                this.renderBoxes()

            }, 5 * 1000)

        }, delayS * 1000)
    }


    // Create a shuffled list of numbers 0-99 that will be used to render the boxed in deterministic & semi random order.
    private initBoxIndices(iceId: string) {
        this.boxIndices = Array.from({length: 100}, (_, i) => i);
        const seed = Math.abs(hashCode(iceId)) % 1000
        deterministicShuffle(this.boxIndices, seed)
    }

    renderBoxes() {
        const unitsPerBox = Math.floor(this.totalUnits / 100)

        let unitCount = 0
        for (let i = 0; i < 100; i++) {
            const index = this.boxIndices[i]
            // const index = i
            const x = index % 10
            const y = Math.floor(index / 10)

            let fillFraction
            if (unitCount + unitsPerBox <= this.unitsHacked) {
                fillFraction = 1
            }
            else if (unitCount > (this.unitsHacked)) {
                fillFraction = 0
            }
            else {
                fillFraction = (this.unitsHacked % unitsPerBox) / unitsPerBox
            }

            if (this.boxDisplaysByIndex[index]) {
                this.boxDisplaysByIndex[index].updateFillFraction(fillFraction)
            } else {
                this.boxDisplaysByIndex[index] = new SlowIceBlockDisplay(x, y, fillFraction, this.canvas)
            }

            unitCount += unitsPerBox
        }
        this.canvas.renderAll()
    }
}

export const slowIceCanvas = new SlowIceCanvas()