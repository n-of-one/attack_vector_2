import {Dispatch, Store} from "redux";
import {Application, Container, FederatedPointerEvent, FederatedWheelEvent, Rectangle, Sprite, Texture,} from "pixi.js";
import {JigsawEnterData, PieceGroup} from "../JigsawServerActionProcessor";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";
import {SnapGroupDisplay} from "./SnapGroupDisplay";
import {calculatePieceDimensions, IMAGE_HEIGHT, IMAGE_WIDTH, PUZZLE_SCALE} from "../component/JigsawShapes";
import {LoadedMedia} from "../JigsawIceManager";

export const CANVAS_HEIGHT = 928;
export const CANVAS_WIDTH = 1880;

export class JigsawCanvas {

    private readonly app: Application
    private readonly store: Store
    private readonly dispatch: Dispatch
    private readonly piecesByLocation: Map<string, JigsawPieceDisplay>

    private readonly piecesLayer: Container
    private readonly topLayer: Container

    private rotating: boolean = false
    private draggingGroup: SnapGroupDisplay | null = null
    private dragOffsetX: number = 0
    private dragOffsetY: number = 0

    static async create(
        data: JigsawEnterData, dispatch: Dispatch, store: Store, media: LoadedMedia,
    ): Promise<JigsawCanvas> {
        const canvasEl = document.getElementById('jigsawCanvas') as HTMLCanvasElement | null
        if (!canvasEl) throw new Error("jigsawCanvas element not found")

        const app = new Application()
        await app.init({
            canvas: canvasEl,
            width: CANVAS_WIDTH,
            height: CANVAS_HEIGHT,
            background: 0x181818,
            antialias: false,
        })
        app.ticker.maxFPS = 30

        return new JigsawCanvas(app, data, dispatch, store, media)
    }

    private constructor(
        app: Application, data: JigsawEnterData, dispatch: Dispatch, store: Store, media: LoadedMedia,
    ) {
        this.app = app
        this.dispatch = dispatch
        this.store = store

        if (media.width < IMAGE_WIDTH || media.height < IMAGE_HEIGHT) {
            throw new Error(
                `Source media (${media.width}x${media.height}) is too small. ` +
                `Minimum size is ${IMAGE_WIDTH}x${IMAGE_HEIGHT}.`
            )
        }

        const bgLayer = new Container()
        this.piecesLayer = new Container()
        this.topLayer = new Container()
        this.app.stage.addChild(bgLayer)
        this.app.stage.addChild(this.piecesLayer)
        this.app.stage.addChild(this.topLayer)

        this.addBackgroundImage(bgLayer, media)

        const puzzleCols = data.columns
        const puzzleRows = data.rows
        const {pieceWidth, pieceHeight} = calculatePieceDimensions(puzzleCols, puzzleRows)

        this.piecesByLocation = new Map(
            data.pieces.map(config => {
                const display = new JigsawPieceDisplay({
                    col: config.col, row: config.row, config,
                    sharedTexture: media.texture,
                    sourceWidth: media.width, sourceHeight: media.height,
                    puzzleCols, puzzleRows, pieceWidth, pieceHeight,
                })
                return [`${config.col}:${config.row}`, display]
            })
        )

        const groupedPieces = this.applyGroups(data.groups)
        for (const piece of this.piecesByLocation.values()) {
            if (!groupedPieces.has(piece)) {
                new SnapGroupDisplay(new Set([piece]), this)
            }
        }

        this.registerEventHandlers()
    }

    get ticker() {
        return this.app.ticker
    }

    addSnapGroup(group: SnapGroupDisplay) {
        this.piecesLayer.addChild(group.container)
    }

    removeSnapGroup(group: SnapGroupDisplay) {
        if (group.container.parent) {
            group.container.parent.removeChild(group.container)
        }
    }

    private applyGroups(groups: PieceGroup[]): Set<JigsawPieceDisplay> {
        const groupedPieces = new Set<JigsawPieceDisplay>()

        for (const group of groups) {
            if (group.length < 2) continue

            const [anchorCol, anchorRow] = group[0]
            const anchor = this.piecesByLocation.get(`${anchorCol}:${anchorRow}`)
            if (!anchor) continue

            const pieces = new Set<JigsawPieceDisplay>()
            pieces.add(anchor)
            groupedPieces.add(anchor)

            for (let i = 1; i < group.length; i++) {
                const [col, row] = group[i]
                const piece = this.piecesByLocation.get(`${col}:${row}`)
                if (!piece) continue

                piece.positionRelativeTo(anchor)
                pieces.add(piece)
                groupedPieces.add(piece)
            }

            const snapGroup = new SnapGroupDisplay(pieces, this)
            snapGroup.updateStrokeOpacity(this.piecesByLocation)
        }

        return groupedPieces
    }

    private addBackgroundImage(backgroundLayer: Container, media: LoadedMedia) {
        const cropOffsetX = (media.width - IMAGE_WIDTH) / 2
        const cropOffsetY = (media.height - IMAGE_HEIGHT) / 2
        const displayWidth = IMAGE_WIDTH * PUZZLE_SCALE
        const displayHeight = IMAGE_HEIGHT * PUZZLE_SCALE

        const pixelSize = 96 * 1.5
        const aspect = IMAGE_WIDTH / IMAGE_HEIGHT
        const tinyWidth = Math.round(aspect >= 1 ? pixelSize : pixelSize * aspect)
        const tinyHeight = Math.round(aspect >= 1 ? pixelSize / aspect : pixelSize)

        const tinyCanvas = document.createElement('canvas')
        tinyCanvas.width = tinyWidth
        tinyCanvas.height = tinyHeight
        const tinyCtx = tinyCanvas.getContext('2d')!
        try {
            tinyCtx.drawImage(media.sourceElement as CanvasImageSource,
                cropOffsetX, cropOffsetY, IMAGE_WIDTH, IMAGE_HEIGHT, 0, 0, tinyWidth, tinyHeight)
        } catch (e) {
            console.warn("Could not snapshot background frame", e)
            return
        }

        const pixelCanvas = document.createElement('canvas')
        pixelCanvas.width = displayWidth
        pixelCanvas.height = displayHeight
        const pixelCtx = pixelCanvas.getContext('2d')!
        pixelCtx.imageSmoothingEnabled = false
        pixelCtx.drawImage(tinyCanvas, 0, 0, displayWidth, displayHeight)

        const texture = Texture.from(pixelCanvas)
        const sprite = new Sprite(texture)
        sprite.alpha = 0.15
        sprite.anchor.set(0.5)
        sprite.position.set(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2)
        backgroundLayer.addChild(sprite)
    }

    private registerEventHandlers() {
        const stage = this.app.stage
        stage.eventMode = 'static'
        stage.hitArea = new Rectangle(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

        const canvasEl = this.app.canvas
        canvasEl.addEventListener('contextmenu', e => e.preventDefault())
        canvasEl.addEventListener('wheel', e => e.preventDefault(), {passive: false})

        stage.on('pointerdown', (ev: FederatedPointerEvent) => {
            const group = this.findGroupAtTarget(ev.target as Container | null)
            if (!group) return
            if (ev.button === 2) {
                this.rotateGroup(group, true)
                return
            }
            if (ev.button !== 0) return
            this.draggingGroup = group
            this.topLayer.addChild(group.container)
            this.dragOffsetX = group.container.position.x - ev.global.x
            this.dragOffsetY = group.container.position.y - ev.global.y
        })

        stage.on('pointermove', (ev: FederatedPointerEvent) => {
            if (!this.draggingGroup) return
            this.draggingGroup.container.position.set(
                ev.global.x + this.dragOffsetX,
                ev.global.y + this.dragOffsetY,
            )
        })

        const endDrag = () => {
            if (!this.draggingGroup) return
            const group = this.draggingGroup
            this.draggingGroup = null
            this.piecesLayer.addChild(group.container)
            group.trySnap(this.piecesByLocation)
        }
        stage.on('pointerup', endDrag)
        stage.on('pointerupoutside', endDrag)

        stage.on('wheel', (ev: FederatedWheelEvent) => {
            const group = this.findGroupAtTarget(ev.target as Container | null)
            if (!group) return
            this.rotateGroup(group, ev.deltaY > 0)
        })
    }

    private findGroupAtTarget(target: Container | null): SnapGroupDisplay | null {
        let node: Container | null = target
        while (node) {
            const sg = (node as any).__snapGroup
            if (sg) return sg as SnapGroupDisplay
            node = node.parent
        }
        return null
    }

    private rotateGroup(snapGroup: SnapGroupDisplay, clockwise: boolean) {
        if (this.rotating) return
        this.rotating = true
        snapGroup.rotate(clockwise, () => {
            this.rotating = false
        })
    }
}
