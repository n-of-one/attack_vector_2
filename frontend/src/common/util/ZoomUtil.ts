import Cookies from "js-cookie";
import {developmentServer} from "./DevEnvironment";

const minWidth = 1920
const minHeight = 1080

export const handleZoom = () => {

    if (developmentServer) {
        // don't zoom during development
        return
    }

    const zoomX = window.screen.width / minWidth
    const zoomY = window.screen.height / minHeight
    const minZoom = Math.min(zoomX, zoomY)

    if (minZoom >= 1) return

    const zoomFactor = Math.ceil(minZoom * 100)
    const zoomStyle = zoomFactor + "%";

    const userAgentString = navigator.userAgent;

    if (userAgentString.indexOf("Chrome") > -1) {
        // Fix zoom for Chrome
        ((document.body.style) as any).zoom = zoomStyle;
    } else {
        const zoomWarning = Cookies.get("zoomWarning")
        if (!zoomWarning) {
            alert(`Please set zoom to ${zoomStyle} or switch to Chrome.

Attack Vector is best played at 1920x1080 with 100% scaling.
You either have a lower resolution or a higher scaling.

As a result you will see scrollbars and some graphics will fall off the screen.

Manually set zoom to ${zoomStyle} to compensate.
`)
            Cookies.set("zoomWarning", "true", {expires: 30});
        }
    }
}
