import {webSocketConnection} from "../server/WebSocketConnection";

export const setFontSize = (size: number) => {
    const clamped = Math.min(14, Math.max(12, size))

    document.documentElement.style.setProperty("--app-font-size", `${clamped}px`)
    document.documentElement.style.setProperty("--app-line-height", `${clamped+2}px`)
}

export const getCurrentFontSize = (): number => {
    const value = getComputedStyle(document.documentElement)
        .getPropertyValue("--app-font-size")
        .trim()

    return parseInt(value.replace("px", ""), 10) || 16
}

const handleFontEvent = (event: KeyboardEvent, userId: string) => {
    if (event.key === "PageUp" && event.shiftKey) {
        event.preventDefault()
        updateFontSize(userId, -1)
    }

    if (event.key === "PageDown"  && event.shiftKey) {
        event.preventDefault()
        updateFontSize(userId, 1)
    }
}

const updateFontSize = (userId: string, delta: number) => {
    const currentSize = getCurrentFontSize()
    const newFontSize = Math.min(14, Math.max(12, currentSize + delta))
    if (currentSize === newFontSize) return

    webSocketConnection.send("/user/edit", {userId: userId, field: "fontSize", value: newFontSize})
}


let listenerAttached = false

export const attachFontChangeListener = (userId: string) => {
    if (listenerAttached) return
    window.addEventListener("keydown", (event) => handleFontEvent(event, userId))
    listenerAttached = true
}
