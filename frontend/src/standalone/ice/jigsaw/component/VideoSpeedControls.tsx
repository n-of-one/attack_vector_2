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
    const [videoElement, setVideoElement] = useState<HTMLVideoElement | null>(null)
    const [speed, setSpeed] = useState<number>(DEFAULT_SPEED)

    // Once the puzzle becomes visible, poll briefly for the loaded video element.
    useEffect(() => {
        if (uiMode === HIDDEN) return
        if (videoElement) return

        const found = jigsawIceManager.getVideoElement()
        if (found) {
            setVideoElement(found)
            return
        }

        const intervalId = window.setInterval(() => {
            const element = jigsawIceManager.getVideoElement()
            if (element) {
                setVideoElement(element)
                window.clearInterval(intervalId)
            }
        }, 200)

        // Give up after 10 seconds (probably a static image, or load failed).
        const timeoutId = window.setTimeout(() => window.clearInterval(intervalId), 10000)

        return () => {
            window.clearInterval(intervalId)
            window.clearTimeout(timeoutId)
        }
    }, [uiMode, videoElement])

    const applySpeed = (newSpeed: number) => {
        if (!videoElement) return
        setSpeed(newSpeed)
        if (newSpeed === 0) {
            videoElement.pause()
            return
        }
        videoElement.playbackRate = newSpeed
        void videoElement.play().catch(() => { /* ignore autoplay rejection */
        })
    }

    if (!videoElement) return null

    return (
        <div className="btn-group btn-group-sm" role="group" aria-label="Video speed" style={{paddingTop: "4px"}}>
            {SPEEDS.map(speedOption => {
                const active = speedOption === speed
                const className = "btn btn-sm " + (active ? "btn-info" : "btn-outline-info")
                return (
                    <button
                        key={speedOption}
                        type="button"
                        className={className}
                        style={{width: "58px"}}
                        onClick={() => applySpeed(speedOption)}
                    >
                        {formatSpeed(speedOption)}
                    </button>
                )
            })}
        </div>
    )
}
