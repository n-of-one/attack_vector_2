import {fabric} from "fabric"
import {calcLine, LinePositions} from "../CanvasUtils"
import {NodeScanType, OUTSIDE_SCAN, SCAN_NODE_DEEP, SCAN_NODE_INITIAL} from "../../../hacker/run/model/NodeScanTypes"
import {Schedule} from "../../util/Schedule"
import {ConnectionVisual} from "../visuals/ConnectionVisual"
import {COLOR_PROBE_LINE} from "./util/DisplayConstants"
import {Canvas} from "fabric/fabric-impl"
import {DisplayCollection} from "./util/DisplayCollection"
import {NodeDisplay} from "./NodeDisplay"
import {Display} from "./Display"
import {ProbeVisual} from "../visuals/ProbeVisual"
import {Timings} from "../../model/Ticks";


export class ProbeDisplay implements Display {

    canvas: Canvas
    id = null
    schedule
    yourProbe
    probeVisual: ProbeVisual | null = null

    connectionVisuals: ConnectionVisual[] = []
    padding: number

    x = 0
    y = 0
    size = 0

    constructor(canvas: Canvas, path: string[], scanType: NodeScanType,
                startDisplay: Display, yourProbe: boolean, nodeDisplays: DisplayCollection<NodeDisplay>,
                timings: Timings) {
        this.canvas = canvas
        this.schedule = new Schedule(null)
        this.yourProbe = yourProbe
        this.padding = yourProbe ? 3: 5

        let currentDisplay: Display = startDisplay
        path.forEach((nodeId) => {
            const nextDisplay = nodeDisplays.get(nodeId)
            this.scheduleMoveStep(nextDisplay, currentDisplay, timings.connection)
            currentDisplay = nextDisplay
        })
        const lastNodeId: string = path.pop()!
        this.schedule.run(0, () => {
            this.processProbeArrive(scanType, lastNodeId, currentDisplay, timings)
        })

    }

    getAllIcons(): fabric.Object[] {
        const objects: fabric.Object[] = this.probeVisual ? [this.probeVisual.image] : []
        this.connectionVisuals.forEach(element => objects.push(element.line))
        return objects
    }

    scheduleMoveStep(nextDisplay: Display, currentDisplay: Display, durationTicks: number) {
        this.schedule.run(22, () => this.moveStep(nextDisplay, currentDisplay, durationTicks))
    }

    moveStep(nextDisplay: Display, currentDisplay: Display, duration: number) {

        const lineData = calcLine(currentDisplay, nextDisplay, this.padding)
        const lineStart = new LinePositions(lineData.line[0], lineData.line[1], lineData.line[0], lineData.line[1])


        const lineElement = new ConnectionVisual(lineStart, COLOR_PROBE_LINE, this.canvas)

        this.connectionVisuals.push(lineElement)
        lineElement.extendTo(lineData, duration)
    }

    processProbeArrive(scanType: NodeScanType, nodeId: string, currentDisplay: Display, timings: Timings) {

        this.probeVisual = new ProbeVisual(this.canvas, currentDisplay as NodeDisplay, this.schedule)

        switch (scanType) {
            case SCAN_NODE_INITIAL:
                return this.scanInside(nodeId, SCAN_NODE_INITIAL, timings)
            case OUTSIDE_SCAN:
                return this.scanOutside(nodeId, timings)
            case SCAN_NODE_DEEP:
                return this.scanInside(nodeId, SCAN_NODE_DEEP, timings)
            default:
                throw new Error("Unknown scantype: " + scanType)
        }
    }

    scanInside(nodeId: string, scanType: NodeScanType, timings: Timings) {
        this.probeVisual!.zoomInAndOutAndRemove(timings)
        this.finishProbe()
    }

    scanOutside(nodeId: string, timings: Timings) {
        this.probeVisual!.zoomOutAndInAndRemove(timings)
        this.finishProbe()
    }

    finishProbe() {
        this.schedule.run(10, () => {
            this.connectionVisuals.forEach(lineElement => {
                lineElement.disappear(10)
            })
        })
        this.schedule.run(0, () => {
            this.probeVisual!.remove()
            this.connectionVisuals.forEach(lineElement => {
                lineElement.remove()
            })
            console.timeEnd("scan")
        })
    }

    remove() {
        this.probeVisual?.remove()
        this.connectionVisuals.forEach(lineElement => {
            lineElement.remove()
        })
    }

    terminate() {
        this.schedule.terminate()
    }
}
