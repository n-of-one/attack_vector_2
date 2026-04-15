import {Texture} from "pixi.js";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {GenericIceManager} from "../common/GenericIceManager";
import {JigsawCanvas} from "./canvas/JigsawCanvas";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalReducer";
import {JIGSAW_BEGIN} from "./reducer/JigsawUiStateReducer";
import {JigsawEnterData} from "./JigsawServerActionProcessor";

export interface LoadedMedia {
    texture: Texture
    sourceElement: HTMLImageElement | HTMLVideoElement
    width: number
    height: number
    /** True for images. For videos, true if the source loops seamlessly; false means we fade the boundary to hide a visible seam. */
    loopsCleanly: boolean
}

function isVideoUrl(url: string): boolean {
    return /\.(mp4|webm|mov|ogv)(\?|$)/i.test(url)
}

/** Load image or video into a Pixi Texture. Returns once dimensions are known. */
async function loadMedia(url: string, loopsCleanly: boolean): Promise<LoadedMedia> {
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
        return {texture, sourceElement: video, width: video.videoWidth, height: video.videoHeight, loopsCleanly}
    }

    const image = new Image()
    image.crossOrigin = 'anonymous'
    image.src = url
    await new Promise<void>((resolve, reject) => {
        image.onload = () => resolve()
        image.onerror = () => reject(new Error(`Failed to load image: ${url}`))
    })
    const texture = Texture.from(image)
    return {texture, sourceElement: image, width: image.naturalWidth, height: image.naturalHeight, loopsCleanly: true}
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

        loadMedia(data.imageSrc, smoothLoop).then(async media => {
            this.media = media
            this.jigsawCanvas = await JigsawCanvas.create(data, this.dispatch, this.store, media)
        }).catch(err => {
            console.error("Failed to load jigsaw source media", err)
        })
    }

    /** Returns the video element backing the puzzle, or null if the media is a static image or not yet loaded. */
    getVideoElement(): HTMLVideoElement | null {
        const el = this.media?.sourceElement
        return el instanceof HTMLVideoElement ? el : null
    }
}

export const jigsawIceManager = new JigsawIceManager();
