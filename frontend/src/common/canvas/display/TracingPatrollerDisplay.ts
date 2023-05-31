import {fabric} from "fabric";
import {animate, calcLine, getHtmlImage, LinePositions} from "../CanvasUtils";
import {Schedule} from "../../util/Schedule";
import {COLOR_PATROLLER_LINE, IMAGE_SIZE, SCALE_NORMAL} from "./util/DisplayConstants";
import {ConnectionVisual} from "../visuals/ConnectionVisual";
import {Display} from "./Display";
import {Canvas} from "fabric/fabric-impl";
import {Dispatch} from "redux";
import {DisplayCollection} from "./util/DisplayCollection";
import {NodeDisplay} from "./NodeDisplay";
import {HackerDisplay} from "./HackerDisplay";
import {PatrollerData} from "../../../hacker/server/RunServerActionProcessor";
import {Timings} from "../../model/Ticks";


export class TracingPatrollerDisplay implements Display {

    canvas : Canvas
    dispatch : Dispatch
    nodeDisplays : DisplayCollection<NodeDisplay>
    hackerDisplays: DisplayCollection<HackerDisplay>

    patrollerId: string | null
    currentNodeId: string
    currentNodeDisplay: Display

    schedule: Schedule

    patrollerIcon: fabric.Image
    lineElements: ConnectionVisual[] = []

    aborted = false;

    x: number
    y: number
    size = 0

    constructor({patrollerId, nodeId, path, timings}: PatrollerData, canvas: Canvas, dispatch: Dispatch,
                nodeDisplays: DisplayCollection<NodeDisplay>,
                hackerDisplays: DisplayCollection<HackerDisplay>) {

        this.patrollerId = patrollerId;
        this.currentNodeId = nodeId;

        this.canvas = canvas;
        this.schedule = new Schedule(dispatch);
        this.dispatch = dispatch;
        this.nodeDisplays = nodeDisplays;
        this.hackerDisplays = hackerDisplays


        this.currentNodeDisplay = nodeDisplays.get(nodeId)

        this.x = this.currentNodeDisplay.x
        this.y = this.currentNodeDisplay.y

        const image = getHtmlImage("PATROLLER_3")
        this.patrollerIcon = new fabric.Image(image, {
            left: this.currentNodeDisplay.x,
            top: this.currentNodeDisplay.y,
            height: IMAGE_SIZE,
            width: IMAGE_SIZE,
            scaleX: SCALE_NORMAL,
            scaleY: SCALE_NORMAL,
            opacity: 0,
            selectable: false,
        });
        this.canvas.add(this.patrollerIcon);
        this.canvas.bringToFront(this.patrollerIcon);

        if (path) {
            path.forEach((segment) => {
                const fromNodeDisplay = this.nodeDisplays.get(segment.fromNodeId);
                const toNodeDisplay = this.nodeDisplays.get(segment.toNodeId);
                const lineEndData = calcLine(fromNodeDisplay, toNodeDisplay, 4);
                const styling = {"opacity": 0}
                const lineElement = new ConnectionVisual(lineEndData, COLOR_PATROLLER_LINE, this.canvas, styling);
                this.lineElements.push(lineElement);

                lineElement.appear(timings.appear * 2);
            });
        }

        animate(this.canvas, this.patrollerIcon, "opacity", 1, timings.appear);
        this.schedule.wait(timings.appear);
    }

    getAllIcons(): fabric.Object[] {
        const objects: fabric.Object[] =  [this.patrollerIcon]
        this.lineElements.forEach( element => objects.push(element.line))
        return objects
    }

    move(fromNodeId: string, toNodeId: string, timings: Timings) {

        this.schedule.run(timings.move, () => {
            const fromNodeDisplay = this.nodeDisplays.get(fromNodeId)
            const toNodeDisplay = this.nodeDisplays.get(toNodeId)

            const lineData = calcLine(fromNodeDisplay, toNodeDisplay, 4);
            const lineStart = new LinePositions(lineData.line[0], lineData.line[1], lineData.line[0], lineData.line[1])

            const lineElement = new ConnectionVisual(lineStart, COLOR_PATROLLER_LINE, this.canvas);
            this.lineElements.push(lineElement);

            lineElement.extendTo(lineData, timings.move);
        });
    }

    lock(hackerId: string) {
        this.hackerDisplays.get(hackerId).lockByPatroller();
    }

    disappear() {
        this.schedule.run(20, () => {
            animate(this.canvas, this.patrollerIcon, "opacity", 0, 20);
            this.lineElements.forEach( (element) => {
                element.disappear(20);
            });
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.patrollerIcon);
            this.lineElements.forEach( (element) => {
                element.remove();
            });
        });
    }

    terminate() {
        this.schedule.terminate()
    }

}