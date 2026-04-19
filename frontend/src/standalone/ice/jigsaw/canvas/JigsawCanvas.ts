import {Dispatch, Store} from "redux";
import {Application, ColorMatrixFilter, Container, FederatedPointerEvent, FederatedWheelEvent, Rectangle, Sprite, Texture,} from "pixi.js";
import {JigsawEnterData, JigsawMovedPayload, JigsawRotatePayload, JigsawSnapPayload} from "../JigsawServerActionProcessor";
import {JigsawPieceDisplay} from "./JigsawPieceDisplay";
import {SnapGroupDisplay} from "./SnapGroupDisplay";
import {calculatePieceDimensions, Group, IMAGE_HEIGHT, IMAGE_WIDTH, PUZZLE_SCALE} from "../component/JigsawShapes";
import {LoadedMedia} from "../JigsawIceManager";
import {webSocketConnection} from "../../../../common/server/WebSocketConnection";
import {ice} from "../../../StandaloneGlobals";

export const CANVAS_HEIGHT = 928;
export const CANVAS_WIDTH = 1880;

export class JigsawCanvas {

    private readonly app: Application
    private readonly store: Store
    private readonly dispatch: Dispatch
    private readonly piecesByLocation: Map<string, JigsawPieceDisplay>
    private readonly groupsById: Map<string, SnapGroupDisplay> = new Map()

    private readonly piecesLayer: Container
    private readonly topLayer: Container

    private rotating: boolean = false
    private draggingGroupId: string | null = null
    private dragOffsetX: number = 0
    private dragOffsetY: number = 0

    static async create(
        data: JigsawEnterData, dispatch: Dispatch, store: Store, media: LoadedMedia,
    ): Promise<JigsawCanvas> {
        const canvasElement = document.getElementById('jigsawCanvas') as HTMLCanvasElement | null
        if (!canvasElement) throw new Error("jigsawCanvas element not found")

        const app = new Application()
        await app.init({
            canvas: canvasElement,
            width: CANVAS_WIDTH,
            height: CANVAS_HEIGHT,
            background: 0x181818,
            antialias: true,
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

        const backgroundLayer = new Container()
        this.piecesLayer = new Container()
        this.topLayer = new Container()
        this.app.stage.addChild(backgroundLayer)
        this.app.stage.addChild(this.piecesLayer)
        this.app.stage.addChild(this.topLayer)

        this.addBackgroundImage(backgroundLayer, media)

        const puzzleColumns = data.columns
        const puzzleRows = data.rows
        const {pieceWidth, pieceHeight} = calculatePieceDimensions(puzzleColumns, puzzleRows)

        this.piecesByLocation = new Map(
            data.pieces.map(config => {
                const display = new JigsawPieceDisplay({
                    column: config.column, row: config.row, config,
                    sharedTexture: media.texture,
                    sourceWidth: media.width, sourceHeight: media.height,
                    puzzleColumns, puzzleRows, pieceWidth, pieceHeight,
                })
                return [`${config.column}:${config.row}`, display]
            })
        )

        this.applyGroups(data.groups)

        this.registerEventHandlers()
        this.installVideoLoopFader(media)
    }

    /**
     * For videos whose first and last frames don't match, we drive the loop
     * manually (loop=false + ended handler) and fade the puzzle pieces to
     * black across the boundary to hide the seam.
     */
    private installVideoLoopFader(media: LoadedMedia) {
        if (!(media.sourceElement instanceof HTMLVideoElement)) return
        if (media.loopsCleanly) return

        const video = media.sourceElement
        const FADE_SECONDS = 0.3
        const FADE_MILLISECONDS = FADE_SECONDS * 1000

        const easeInSine = (t: number) => 1 - Math.cos((t * Math.PI) / 2)
        const easeOutSine = (t: number) => Math.sin((t * Math.PI) / 2)

        const filter = new ColorMatrixFilter()
        this.piecesLayer.filters = [filter]

        // Single state machine. The fade VALUE is always wall-clock-driven so it
        // can't stutter when video.currentTime plateaus, jumps, or there's a gap
        // between the `ended` event and the `seeked` event. The video clock is
        // only used to TRIGGER the fade-out, never to compute its progress.
        type Phase = 'playing' | 'fadingOut' | 'wrapping' | 'fadingIn'
        let phase: Phase = 'playing'
        let phaseStart = 0

        video.addEventListener('ended', () => {
            // fadingOut should already be in progress, but in case `ended` fires
            // before our trigger (short clip, or playbackRate spike) we just go.
            phase = 'wrapping'
            video.currentTime = 0
            void video.play().catch(() => { /* ignore */
            })
        })
        video.addEventListener('seeked', () => {
            // Only react to seeks that happen as part of our manual wrap.
            // User-initiated seeks (e.g. future scrubbing) won't be in 'wrapping'.
            if (phase === 'wrapping') {
                phase = 'fadingIn'
                phaseStart = performance.now()
            }
        })

        this.app.ticker.add(() => {
            const now = performance.now()

            // Trigger fade-out once when we cross into the tail of the video.
            // Backdate phaseStart by how far we already are into the fade window
            // (scaled by playbackRate) so the fade picks up smoothly instead of
            // snapping. After this, `currentTime` is no longer consulted.
            if (phase === 'playing') {
                const duration = video.duration
                if (isFinite(duration) && duration > 0) {
                    const remainingVideoSeconds = duration - video.currentTime
                    const rate = video.playbackRate || 1
                    const remainingWallMilliseconds = (remainingVideoSeconds / rate) * 1000
                    if (remainingWallMilliseconds < FADE_MILLISECONDS) {
                        phase = 'fadingOut'
                        phaseStart = now - (FADE_MILLISECONDS - remainingWallMilliseconds)
                    }
                }
            }

            let fade = 1
            switch (phase) {
                case 'playing':
                    fade = 1
                    break
                case 'fadingOut': {
                    const elapsed = now - phaseStart
                    const t = Math.min(1, elapsed / FADE_MILLISECONDS)
                    fade = 1 - easeInSine(t)
                    break
                }
                case 'wrapping':
                    // Hold black across the gap between `ended` and `seeked`.
                    fade = 0
                    break
                case 'fadingIn': {
                    const elapsed = now - phaseStart
                    if (elapsed >= FADE_MILLISECONDS) {
                        phase = 'playing'
                        fade = 1
                    } else {
                        fade = easeOutSine(elapsed / FADE_MILLISECONDS)
                    }
                    break
                }
            }
            filter.brightness(fade, false)
        })
    }

    get ticker() {
        return this.app.ticker
    }

    addSnapGroup(group: SnapGroupDisplay) {
        this.piecesLayer.addChild(group.container)
        this.groupsById.set(group.id, group)
    }

    removeSnapGroup(group: SnapGroupDisplay) {
        if (group.container.parent) {
            group.container.parent.removeChild(group.container)
        }
        if (this.groupsById.get(group.id) === group) {
            this.groupsById.delete(group.id)
        }
    }

    private applyGroups(groups: Group[]) {
        for (const group of groups) {
            const pieces = new Set<JigsawPieceDisplay>()

            const anchorLocation = group.pieces[0]
            if (!anchorLocation) continue
            const anchor = this.piecesByLocation.get(`${anchorLocation.column}:${anchorLocation.row}`)
            if (!anchor) continue

            // Set anchor's logical rotation; the snap group container holds the actual rotation.
            anchor.rotation = group.rotation
            pieces.add(anchor)

            for (let i = 1; i < group.pieces.length; i++) {
                const {column, row} = group.pieces[i]
                const piece = this.piecesByLocation.get(`${column}:${row}`)
                if (!piece) continue
                piece.positionRelativeTo(anchor)
                pieces.add(piece)
            }

            const snapGroup = new SnapGroupDisplay(group.id, pieces, this)
            // Constructor's recomputePivot put pivot = position = local centroid. Move the group
            // so that centroid sits at the server-provided (group.x, group.y), then apply rotation.
            snapGroup.container.position.set(group.x, group.y)
            snapGroup.container.rotation = (group.rotation * Math.PI) / 180
            snapGroup.updateStrokeOpacity(this.piecesByLocation)
        }
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
        } catch (error) {
            console.warn("Could not snapshot background frame", error)
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

        const canvasElement = this.app.canvas
        canvasElement.addEventListener('contextmenu', event => event.preventDefault())
        canvasElement.addEventListener('wheel', event => event.preventDefault(), {passive: false})

        stage.on('pointerdown', (event: FederatedPointerEvent) => {
            const group = this.findGroupAtTarget(event.target as Container | null)
            if (!group) return
            if (event.button === 2) {
                this.rotateGroup(group, true)
                return
            }
            if (event.button !== 0) return
            this.draggingGroupId = group.id
            this.topLayer.addChild(group.container)
            this.dragOffsetX = group.container.position.x - event.global.x
            this.dragOffsetY = group.container.position.y - event.global.y
        })

        stage.on('pointermove', (event: FederatedPointerEvent) => {
            if (!this.draggingGroupId) return
            const group = this.groupsById.get(this.draggingGroupId)
            if (!group) return
            group.container.position.set(
                event.global.x + this.dragOffsetX,
                event.global.y + this.dragOffsetY,
            )
        })

        const endDrag = () => {
            if (!this.draggingGroupId) return
            const groupId = this.draggingGroupId
            const group = this.groupsById.get(groupId)
            this.draggingGroupId = null
            if (!group) return
            this.piecesLayer.addChild(group.container)
            webSocketConnection.sendObject("/ice/jigsaw/moved", {
                iceId: ice.id,
                groupId,
                x: group.container.position.x,
                y: group.container.position.y,
            })
        }
        stage.on('pointerup', endDrag)
        stage.on('pointerupoutside', endDrag)

        stage.on('wheel', (event: FederatedWheelEvent) => {
            const group = this.findGroupAtTarget(event.target as Container | null)
            if (!group) return
            this.rotateGroup(group, event.deltaY > 0)
        })
    }

    private findGroupAtTarget(target: Container | null): SnapGroupDisplay | null {
        let node: Container | null = target
        while (node) {
            const snapGroup = (node as any).__snapGroup
            if (snapGroup) return snapGroup as SnapGroupDisplay
            node = node.parent
        }
        return null
    }

    private rotateGroup(snapGroup: SnapGroupDisplay, clockwise: boolean) {
        if (this.rotating) return
        this.rotating = true
        const groupId = snapGroup.id
        snapGroup.rotate(clockwise, () => {
            this.rotating = false
            const degrees = (snapGroup.container.rotation * 180) / Math.PI
            const normalized = ((Math.round(degrees) % 360) + 360) % 360
            webSocketConnection.sendObject("/ice/jigsaw/rotate", {
                iceId: ice.id,
                groupId,
                rotation: normalized,
            })
        })
    }

    /** Inbound: server confirmed/initiated a move of a group. */
    onGroupMoved(data: JigsawMovedPayload) {
        if (this.draggingGroupId === data.groupId) return
        const group = this.groupsById.get(data.groupId)
        if (!group) return
        // Pixi semantics: pivot is a point in local (untransformed) coords; position is where
        // that point lands in the parent. Moving the group changes position, not pivot.
        group.container.position.set(data.x, data.y)
    }

    /** Inbound: server confirmed/initiated a rotation of a group. */
    onGroupRotated(data: JigsawRotatePayload) {
        const group = this.groupsById.get(data.groupId)
        if (!group) return
        const currentDegrees = ((Math.round((group.container.rotation * 180) / Math.PI) % 360) + 360) % 360
        if (currentDegrees === data.rotation) return
        if (this.rotating) return
        this.rotating = true
        // Decide direction: rotate the shortest path (+90 = clockwise, -90 = counter).
        const difference = ((data.rotation - currentDegrees) + 360) % 360
        const clockwise = difference === 90 || difference === 180 // 180 we also do clockwise (two successive rotations not handled; server is the source of truth)
        group.rotate(clockwise, () => {
            this.rotating = false
            // If still off by 180, do a second rotation to land exactly. Avoids sending any outbound frame.
            const afterDegrees = ((Math.round((group.container.rotation * 180) / Math.PI) % 360) + 360) % 360
            if (afterDegrees !== data.rotation) {
                this.rotating = true
                group.rotate(clockwise, () => {
                    this.rotating = false
                })
            }
        })
    }

    /** Inbound: server fused groups together. Rebuild surviving group from scratch. */
    onSnap(data: JigsawSnapPayload) {
        // Cancel any active local drag that involves a participating group.
        if (this.draggingGroupId === data.survivingGroupId ||
            (this.draggingGroupId && data.absorbedGroupIds.includes(this.draggingGroupId))) {
            this.draggingGroupId = null
        }

        const participatingIds = [data.survivingGroupId, ...data.absorbedGroupIds]
        const allPieces = new Set<JigsawPieceDisplay>()

        for (const id of participatingIds) {
            const group = this.groupsById.get(id)
            if (!group) continue
            // Kill any in-flight rotation tween on a participating group before we destroy its
            // container — the tween's onComplete would otherwise fire on a dead container and
            // cause `rotating = true` forever.
            group.cancelRotation()
            for (const piece of group.pieces) allPieces.add(piece)
            // Detach pieces from old container so we can hand them to a fresh group.
            for (const piece of group.pieces) group.container.removeChild(piece.container)
            this.removeSnapGroup(group)
            group.container.destroy({children: false})
        }
        this.rotating = false

        // Choose an anchor from the server-provided pieces list so we position using the
        // authoritative grid layout (ignoring any local pre-snap offset drift).
        const anchorLocation = data.pieces[0]
        const anchor = anchorLocation ? this.piecesByLocation.get(`${anchorLocation.column}:${anchorLocation.row}`) : undefined
        if (!anchor) return

        anchor.rotation = data.rotation

        for (const location of data.pieces) {
            const piece = this.piecesByLocation.get(`${location.column}:${location.row}`)
            if (!piece || piece === anchor) continue
            piece.positionRelativeTo(anchor)
        }

        const newGroup = new SnapGroupDisplay(data.survivingGroupId, allPieces, this)
        // Constructor's recomputePivot placed pivot=position=local centroid; shift/rotate to server state.
        newGroup.container.position.set(data.x, data.y)
        newGroup.container.rotation = (data.rotation * Math.PI) / 180
        newGroup.updateStrokeOpacity(this.piecesByLocation)
    }
}
