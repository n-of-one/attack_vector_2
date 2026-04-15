import React, {useEffect, useState} from "react"
import {useSelector} from "react-redux"
import {JigsawRootState} from "../reducer/JigsawRootReducer"
import {HIDDEN} from "../../common/IceModel"
import {jigsawIceManager} from "../JigsawIceManager"

const SPEEDS: number[] = [0, 0.25, 0.5, 1]
const DEFAULT_SPEED = 1

const formatSpeed = (speed: number): string => {
    if (speed === 0) return "0×"
    if (speed === 1) return "1×"
    return `${speed}×`
}

export const VideoSpeedControls = () => {
    const uiMode = useSelector((state: JigsawRootState) => state.ui.mode)
    const [videoEl, setVideoEl] = useState<HTMLVideoElement | null>(null)
    const [speed, setSpeed] = useState<number>(DEFAULT_SPEED)

    // Once the puzzle becomes visible, poll briefly for the loaded video element.
    useEffect(() => {
        if (uiMode === HIDDEN) return
        if (videoEl) return

        const found = jigsawIceManager.getVideoElement()
        if (found) {
            setVideoEl(found)
            return
        }

        const intervalId = window.setInterval(() => {
            const el = jigsawIceManager.getVideoElement()
            if (el) {
                setVideoEl(el)
                window.clearInterval(intervalId)
            }
        }, 200)

        // Give up after 10 seconds (probably a static image, or load failed).
        const timeoutId = window.setTimeout(() => window.clearInterval(intervalId), 10000)

        return () => {
            window.clearInterval(intervalId)
            window.clearTimeout(timeoutId)
        }
    }, [uiMode, videoEl])

    const applySpeed = (newSpeed: number) => {
        if (!videoEl) return
        setSpeed(newSpeed)
        if (newSpeed === 0) {
            videoEl.pause()
            return
        }
        videoEl.playbackRate = newSpeed
        void videoEl.play().catch(() => { /* ignore autoplay rejection */
        })
    }

    if (!videoEl) return null

    return (
        <div className="btn-group btn-group-sm" role="group" aria-label="Video speed" style={{paddingTop: "4px"}}>
            {SPEEDS.map(s => {
                const active = s === speed
                const className = "btn btn-sm " + (active ? "btn-info" : "btn-outline-info")
                return (
                    <button
                        key={s}
                        type="button"
                        className={className}
                        style={{width: "58px"}}
                        onClick={() => applySpeed(s)}
                    >
                        {formatSpeed(s)}
                    </button>
                )
            })}
        </div>
    )
}
