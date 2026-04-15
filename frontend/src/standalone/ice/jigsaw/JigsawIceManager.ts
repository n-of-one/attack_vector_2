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
}

function isVideoUrl(url: string): boolean {
    return /\.(mp4|webm|mov|ogv)(\?|$)/i.test(url)
}

/** Load image or video into a Pixi Texture. Returns once dimensions are known. */
async function loadMedia(url: string): Promise<LoadedMedia> {
    if (isVideoUrl(url)) {
        const video = document.createElement('video')
        video.src = url
        video.muted = true
        video.loop = true
        video.playsInline = true
        video.autoplay = true
        video.crossOrigin = 'anonymous'
        await new Promise<void>((resolve, reject) => {
            video.oncanplay = () => resolve()
            video.onerror = () => reject(new Error(`Failed to load video: ${url}`))
        })
        void video.play().catch(() => { /* autoplay may be blocked; texture still shows first frame */
        })
        const texture = Texture.from(video)
        return {texture, sourceElement: video, width: video.videoWidth, height: video.videoHeight}
    }

    const image = new Image()
    image.crossOrigin = 'anonymous'
    image.src = url
    await new Promise<void>((resolve, reject) => {
        image.onload = () => resolve()
        image.onerror = () => reject(new Error(`Failed to load image: ${url}`))
    })
    const texture = Texture.from(image)
    return {texture, sourceElement: image, width: image.naturalWidth, height: image.naturalHeight}
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

        loadMedia(data.imageSrc).then(async media => {
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
