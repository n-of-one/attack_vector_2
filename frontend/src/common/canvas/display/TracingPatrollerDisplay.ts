import {fabric} from "fabric";
import {animate, getHtmlImage} from "../CanvasUtils";
import {Schedule} from "../../util/Schedule";
import {IMAGE_SIZE, SCALE_NORMAL} from "./util/DisplayConstants";
import {Display} from "./Display";
import {Canvas} from "fabric/fabric-impl";
import {Dispatch} from "redux";
import {DisplayCollection} from "./util/DisplayCollection";
import {NodeDisplay} from "./NodeDisplay";
import {HackerDisplay} from "./HackerDisplay";
import {PatrollerData} from "../../../hacker/RunServerActionProcessor";


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


    x: number
    y: number
    size = 0

    constructor({patrollerId, nodeId, timings}: PatrollerData, canvas: Canvas, dispatch: Dispatch,
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


        animate(this.canvas, this.patrollerIcon, "opacity", 1, timings.appear);
        this.schedule.wait(timings.appear);
    }

    getAllIcons(): fabric.Object[] {
        const objects: fabric.Object[] =  [this.patrollerIcon]
        return objects
    }


    disappear() {
        this.schedule.run(20, () => {
            animate(this.canvas, this.patrollerIcon, "opacity", 0, 20);
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.patrollerIcon);
        });
    }

    terminate() {
        this.schedule.terminate()
    }

}
