import {Renderer, Texture} from "pixi.js";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {JigsawCanvas} from "./canvas/JigsawCanvas";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {JIGSAW_BEGIN} from "./reducer/JigsawUiStateReducer";
import {JigsawEnterData, JigsawMovedPayload, JigsawRotatePayload, JigsawSnapPayload} from "./JigsawServerActionProcessor";
import {ShaderAnimation} from "./shader/ShaderAnimation";

/** Controls the animation speed of animated media. Speed 0 pauses. */
export interface MediaPlayback {
    setSpeed(speed: number): void
}

export interface LoadedMedia {
    texture: Texture
    /** Null for shaders, which render into a Pixi RenderTexture. */
    sourceElement: HTMLImageElement | HTMLVideoElement | null
    width: number
    height: number
    /** True for images. For videos, true if the source loops seamlessly; false means we fade the boundary to hide a visible seam. */
    loopsCleanly: boolean
    /** Null for static images. */
    playback: MediaPlayback | null
    /** Called every ticker frame for media that must render itself (shaders). Video textures update on their own. */
    advanceFrame: ((renderer: Renderer, deltaMilliseconds: number) => void) | null
    /** The current frame, for drawing the pixelated background. */
    snapshot: (renderer: Renderer) => CanvasImageSource
}

const SHADER_PREFIX = "shader:"

function isVideoUrl(url: string): boolean {
    return /\.(mp4|webm|mov|ogv)(\?|$)/i.test(url)
}

function videoPlayback(video: HTMLVideoElement): MediaPlayback {
    return {
        setSpeed(speed: number) {
            if (speed === 0) {
                video.pause()
                return
            }
            video.playbackRate = speed
            void video.play().catch(() => { /* ignore autoplay rejection */
            })
        }
    }
}

/** Load image, video or shader (`shader:<name>`) into a Pixi Texture. Returns once dimensions are known. */
async function loadMedia(url: string, loopsCleanly: boolean): Promise<LoadedMedia> {
    if (url.startsWith(SHADER_PREFIX)) {
        const animation = new ShaderAnimation(url.substring(SHADER_PREFIX.length))
        return {
            texture: animation.texture,
            sourceElement: null,
            width: animation.texture.width,
            height: animation.texture.height,
            loopsCleanly: true,
            playback: animation,
            advanceFrame: (renderer, deltaMilliseconds) => animation.advance(renderer, deltaMilliseconds),
            snapshot: renderer => animation.snapshot(renderer),
        }
    }

    if (isVideoUrl(url)) {
        const video = document.createElement('video')
        video.muted = true
        video.defaultMuted = true
        video.loop = loopsCleanly
        video.playsInline = true
        video.autoplay = true
        video.crossOrigin = 'anonymous'
        video.src = url
        await new Promise<void>((resolve, reject) => {
            video.oncanplay = () => resolve()
            video.onerror = () => reject(new Error(`Failed to load video: ${url}`))
        })
        try {
            await video.play()
        } catch (e) {
            console.warn("Video autoplay rejected; puzzle will show first frame until user interaction.", e)
        }
        const texture = Texture.from(video)
        return {
            texture, sourceElement: video, width: video.videoWidth, height: video.videoHeight, loopsCleanly,
            playback: videoPlayback(video), advanceFrame: null, snapshot: () => video,
        }
    }

    const image = new Image()
    image.crossOrigin = 'anonymous'
    image.src = url
    await new Promise<void>((resolve, reject) => {
        image.onload = () => resolve()
        image.onerror = () => reject(new Error(`Failed to load image: ${url}`))
    })
    const texture = Texture.from(image)
    return {
        texture, sourceElement: image, width: image.naturalWidth, height: image.naturalHeight, loopsCleanly: true,
        playback: null, advanceFrame: null, snapshot: () => image,
    }
}

/** On /dev/ pages, `?media=shader:warp` (or any image/video path) overrides the server-chosen media, for experimenting. */
function devMediaOverride(): string | null {
    if (!window.location.pathname.startsWith("/dev/")) return null
    return new URLSearchParams(window.location.search).get("media")
}

class JigsawIceManager extends GenericIceManager {

    solved: boolean = false

    jigsawCanvas: JigsawCanvas | null = null
    media: LoadedMedia | null = null

    enter(data: JigsawEnterData) {
        if (data.hacked) {
            this.enterHacked()
            return
        }

        this.schedule.clear();
        this.dispatch({type: TERMINAL_CLEAR, terminalId: ICE_DISPLAY_TERMINAL_ID});

        this.displayTerminal(0, "↼ Manual reconstruction interface [info]online");
        this.schedule.dispatch(0, {type: JIGSAW_BEGIN});

        // TODO: replace hardcoded `true` with a per-video flag from the server
        // (e.g. data.smoothLoop). False will engage the fade-to-black boundary
        // handler in JigsawCanvas for clips whose end doesn't match their start.
        const smoothLoop = false

        const imageSource = devMediaOverride() ?? data.imageSource

        loadMedia(imageSource, smoothLoop).then(async media => {
            this.media = media
            this.jigsawCanvas = await JigsawCanvas.create(data, this.dispatch, this.store, media)
        }).catch(error => {
            console.error("Failed to load jigsaw source media", error)
        })
    }

    /** Returns the playback control of the puzzle media, or null if the media is a static image or not yet loaded. */
    getPlayback(): MediaPlayback | null {
        return this.media?.playback ?? null
    }

    onGroupMoved(data: JigsawMovedPayload) {
        this.jigsawCanvas?.onGroupMoved(data)
    }

    onGroupRotated(data: JigsawRotatePayload) {
        this.jigsawCanvas?.onGroupRotated(data)
    }

    onSnap(data: JigsawSnapPayload) {
        this.jigsawCanvas?.onSnap(data)
    }

    iceHacked() {
        this.solved = true
        // performance.now() counts from page load.
        const totalSeconds = Math.floor(performance.now() / 1000)
        const minutes = Math.floor(totalSeconds / 60)
        const seconds = totalSeconds % 60
        // Defer so the final snap is rendered before the blocking alert.
        window.setTimeout(() => alert(`Puzzle solved in ${minutes}m ${seconds.toString().padStart(2, "0")}s`), 300)
    }
}

export const jigsawIceManager = new JigsawIceManager();
