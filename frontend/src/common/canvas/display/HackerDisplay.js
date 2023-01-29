import {fabric} from "fabric";
import {animate, calcLine, calcLineStart, calcLineWithOffset, easeLinear, easeOutSine} from "../CanvasUtils";
import Schedule from "../../Schedule";
import {
    IDENTIFICATION_SCALE_LARGE,
    IDENTIFICATION_SCALE_NORMAL,
    OFFSET,
    COLOR_PATROLLER_LINE,
    SCALE_LARGE,
    SCALE_NORMAL,
    SCALE_SMALL,
    TICKS_HACKER_MOVE_MAIN, COLOR_HACKER_LINE, IMAGE_SIZE
} from "./util/DisplayConstants";
import LineElement from "./util/LineElement";
import {HACKER_RUN_ACTIVITY_MOVING, HACKER_RUN_ACTIVITY_SCANNING, HACKER_RUN_ACTIVITY_STARTING} from "../../enums/HackerState";
import {SERVER_TERMINAL_RECEIVE, TERMINAL_LOCK, TERMINAL_UNLOCK} from "../../terminal/TerminalReducer";

const APPEAR_TIME = 20;
const DISAPPEAR_TIME = 10;


const IDENTIFIER_OPACITY_SCANNING = 0.1;
const IDENTIFIER_OPACITY_HACKING = 0.5;

const LINE_OPACITY_SCANNING = 0.5;
const LINE_OPACITY_HACKING = 1;


export default class HackerDisplay {

    canvas = null;
    schedule = null;
    dispatch = null;
    displayById = null;

    hackerIcon = null;
    hackerIdentifierIcon = null;
    hackerHider = null;
    startLineIcon = null;
    labelIcon = null;
    moveLineElement = null;

    hackerData = null;
    you = false;
    siteStartNodeDisplay = null;
    currentNodeDisplay = null;
    targetNodeDisplay = null;
    y = null;
    x = null;
    locked = false;
    hooked = false;

    constructor(canvas, siteStartNodeDisplay, hackerData, offset, you, dispatch, displayById) {
        this.canvas = canvas;
        this.schedule = new Schedule();
        this.hackerData = hackerData;

        this.you = you;
        this.siteStartNodeDisplay = siteStartNodeDisplay;

        this.dispatch = dispatch;
        this.displayById = displayById;

        this.x = offset;
        this.y = 810 - 20;

        this.addHackerIdentificationIcons();


        let identifierOppacity, lineOpacity;

        if (hackerData.activity === HACKER_RUN_ACTIVITY_SCANNING) {
            this.currentNodeDisplay = null;

            identifierOppacity =  IDENTIFIER_OPACITY_SCANNING;
            lineOpacity = LINE_OPACITY_SCANNING

        } else {
            this.addHackingHacker(hackerData);

            identifierOppacity =  IDENTIFIER_OPACITY_HACKING;
            lineOpacity = LINE_OPACITY_HACKING
        }

        animate(this.canvas, this.hackerIdentifierIcon, "opacity", identifierOppacity, APPEAR_TIME);
        animate(this.canvas, this.startLineIcon, "opacity", lineOpacity, 40);
        animate(this.canvas, this.labelIcon, "opacity", lineOpacity, APPEAR_TIME);

        this.schedule.wait(3);
    }

    addHackingHacker(hackerData) {
        // If the hacker is in transit or still starting the run, then we are displaying them as arrived at the node.
        const moveIncomplete = (hackerData.activity === HACKER_RUN_ACTIVITY_STARTING || hackerData.activity === HACKER_RUN_ACTIVITY_MOVING);
        const nodeId = (moveIncomplete) ? hackerData.targetNodeId : hackerData.nodeId;

        this.currentNodeDisplay = this.displayById[nodeId];

        const {xOffset, yOffset} = this.processOffset(this.currentNodeDisplay, moveIncomplete);
        this.hackerIcon = this.createHackerIcon(SCALE_NORMAL, 1, this.currentNodeDisplay, xOffset, yOffset);
        this.canvas.add(this.hackerIcon);

        this.locked = hackerData.locked;
        if (this.locked) {
            this.lockByPatroller()
        }
    }

    createHackerIcon(scale, opacity, position, offsetX, offsetY) {
        offsetX = (offsetX) ? offsetX : 0;
        offsetY = (offsetY) ? offsetY : 0;

        const image = document.getElementById(this.hackerData.icon);

        const icon = new fabric.Image(image, {
            left: position.x + offsetX,
            top: position.y + offsetY,
            height: IMAGE_SIZE,
            width: IMAGE_SIZE,
            scaleX: scale,
            scaleY: scale,
            opacity: opacity,
            selectable: false,
            hoverCursor: "default",
        });

        return icon
    }

    addHackerIdentificationIcons() {
        const size = this.you ? IDENTIFICATION_SCALE_LARGE : IDENTIFICATION_SCALE_NORMAL;
        this.hackerIdentifierIcon = this.createHackerIcon(size, 0, this);
        this.hackerHider = new fabric.Rect({
            left: this.x,
            top: this.y + 25,
            height: 35,
            width: 40,
            opacity: 1,
            fill: "#333333",
            selectable: false,
            hoverCursor: "default",
        });
        const hackerName = this.you ? "You" : this.hackerData.userName;
        this.labelIcon = new fabric.Text(hackerName, {
            fill: "#f0ad4e",    // color-ok
            fontFamily: "SpaceMono",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            left: this.x,
            top: this.y + 15,
            textAlign: "center", // "center", "right" or "justify".
            opacity: 0,
            selectable: false,
            hoverCursor: "default",
        });
        const lineData = calcLine(this, this.siteStartNodeDisplay);
        this.startLineIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#bb8",
                strokeWidth: 1,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });

        this.canvas.add(this.hackerIdentifierIcon);
        this.canvas.add(this.hackerHider);
        this.canvas.add(this.labelIcon);
        this.canvas.add(this.startLineIcon);
        this.canvas.sendToBack(this.startLineIcon);

    }

    size() {
        return 30;
    }

    repositionHackerIdentification(newX) {
        this.schedule.run(APPEAR_TIME, () => {
            this.x = newX;
            animate(this.canvas, this.hackerIdentifierIcon, "left", newX, APPEAR_TIME);
            animate(this.canvas, this.hackerHider, "left", newX, APPEAR_TIME);
            animate(this.canvas, this.labelIcon, "left", newX, APPEAR_TIME);
            animate(this.canvas, this.hackerHider, "left", newX, APPEAR_TIME);
            const lineData = calcLine(this, this.siteStartNodeDisplay);
            animate(this.canvas, this.startLineIcon, null, lineData.asCoordinates(), APPEAR_TIME);
        });
    }

    disappear() {
        this.schedule.run(DISAPPEAR_TIME, () => {
            if (this.currentNodeDisplay) {
                this.currentNodeDisplay.unregisterHacker(this);
            }
            animate(this.canvas, this.hackerIdentifierIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.startLineIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.labelIcon, "opacity", 0, DISAPPEAR_TIME);
            if (this.hackerIcon) {
                animate(this.canvas, this.hackerIcon, "opacity", 0, DISAPPEAR_TIME);
            }
            if (this.lockIcon) {
                animate(this.canvas, this.lockIcon, 'opacity', 0, DISAPPEAR_TIME);
            }
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.hackerIdentifierIcon);
            this.canvas.remove(this.startLineIcon);
            this.canvas.remove(this.labelIcon);
            this.canvas.remove(this.hackerHider);
            if (this.hackerIcon) {
                this.canvas.remove(this.hackerIcon);
            }
            if (this.lockIcon) {
                this.canvas.remove(this.lockIcon);
            }
        });
    }

    startRun(quick) {
        if (quick) {
            this.startRunQuick()
        } else {
            this.startRunSlow()
        }
    }

    startRunQuick() {
        this.hackerIcon = this.createHackerIcon(SCALE_NORMAL, 1, this.siteStartNodeDisplay);
        this.canvas.add(this.hackerIcon);
        this.canvas.bringToFront(this.hackerIcon);
        animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 10);
        animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, 10);
        animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, 10);

        this.schedule.run(0, () => {
            if (this.you) {
                this.echo(0, "[info]Persona established, hack started.");
            }
            animate(this.canvas, this.hackerIcon, 'opacity', 1, 5);
        });

    }


    startRunSlow() {
        this.hackerIcon = this.createHackerIcon(IDENTIFICATION_SCALE_LARGE, 0, this);
        this.canvas.add(this.hackerIcon);
        this.canvas.sendToBack(this.hackerIcon);

        this.schedule.run(0, () => {
            this.moveStep(this.siteStartNodeDisplay, 0, 0, 200, easeLinear);
            this.animateZoom(SCALE_NORMAL, 100);
            this.animateOpacity(0.7, 20);

            animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 200);
            animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, 100);
            animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, 100);

        });

        if (this.you) {
            this.dispatch({type: TERMINAL_LOCK, terminalId: "main"});
            const random = (max) => {
                return Math.floor(Math.random() * max);
            };
            const personaId = "" + random(10) + random(10) + random(10) + random(10) + random(10) + random(10) + '-' +
                random(10) + random(10) + random(10) + random(10) + '/' + random(10);

            this.echo(20, "");
            this.echo(20, "Persona v2.3 booting");
            this.echo(10, "- unique ID: " + personaId);
        } else {
            this.schedule.wait(50);
        }
        this.schedule.run(0, () => {
            this.animateOpacity(0.1, 100, easeOutSine);
            this.canvas.bringToFront(this.hackerIcon);
        });

        if (this.you) {
            this.echo(10, "- Matching fingerprint with OS deamon");
            this.echo(10, "  - [ok]ok[/] Suppressing persona signature");
            this.echo(10, "  - [ok]ok[/] Connection bandwidth adjusted");
            this.echo(10, "  - [ok]ok[/] Content masked.");
            this.echo(30, "  - [ok]ok[/] Operating speed reduced to mimic OS deamon");
            this.echo(30, "  - [ok]ok[/] Network origin obfuscated ");
        } else {
            this.schedule.wait(100);
        }

        this.schedule.run(0, () => {
            this.animateOpacity(1, 100);
        });

        if (this.you) {
            this.echo(20, "- Persona creation [info]complete");
            this.echo(0, "");
            this.echo(80, "Entering node");
            this.echo(0, "Persona accepted by node OS.");
            this.schedule.run(0, () => {
                this.dispatch({type: TERMINAL_UNLOCK, terminalId: "main"});
            });
        } else {
            this.schedule.wait(100);
        }

        // this.schedule.run(0, () => {
        //     this.canvas.bringToFront(this.hackerIcon);
        // });
    }

    echo(time, message) {
        this.schedule.run(time, () => {
            this.dispatch({type: SERVER_TERMINAL_RECEIVE, data: {terminalId: "main", lines: [message]}});
        });
    }

    moveStep(node, offsetX, offsetY, time, easing) {
        animate(this.canvas, this.hackerIcon, "left", node.x + offsetX, time, easing);
        animate(this.canvas, this.hackerIcon, "top", node.y + offsetY, time, easing);
    }

    moveStart(nodeDisplay, ticks) {
        this.targetNodeDisplay = nodeDisplay;
        this.schedule.run(ticks.start, () => {
            this.currentNodeDisplay.unregisterHacker(this);
            this.moveStep(this.currentNodeDisplay, 0, 0, ticks.start);
        });
        this.schedule.run(ticks.main, () => {
            this.moveStep(nodeDisplay, 0, 0, ticks.main);
            if (this.you) {
                this.moveLineElement = this.animateMoveStepLine(this.currentNodeDisplay, nodeDisplay, ticks.main);
            }
        });
    }

    hookByPatroller() {
        this.hooked = true;
        if (this.moveLineElement) {
            this.moveLineElement.setColor(COLOR_PATROLLER_LINE);
        }
    }

    snapBack(ticks) {
        this.schedule.run(ticks.snapBack, () => {
            this.moveStep(this.currentNodeDisplay, 0, 0, ticks.snapBack);

            if (this.you) {
                const lineStartData = calcLineStart(this.currentNodeDisplay, this.targetNodeDisplay, 0, 0);
                this.moveLineElement.extendTo(lineStartData, TICKS_HACKER_MOVE_MAIN, fabric.util.ease.easeInOutSine);
            }
        });
        this.schedule.run(ticks.arrive, () => {
            const {xOffset, yOffset} = this.processOffset(this.currentNodeDisplay);
            this.moveStep(this.currentNodeDisplay, xOffset, yOffset, ticks.arrive);
        });
    }

    moveArrive(nodeDisplay) {
        this.currentNodeDisplay = nodeDisplay;
        this.targetNodeDisplay = null;
        this.schedule.run(4, () => {

            const {xOffset, yOffset} = this.processOffset(nodeDisplay);
            this.moveStep(nodeDisplay, xOffset, yOffset, 4);
        });
    }

    processOffset(nodeDisplay, moveIncomplete) {
        if (moveIncomplete) {
            return {xOffset: 0, yOffset: 0}
        }

        if (this.you) {
            return {xOffset: OFFSET, yOffset: OFFSET};
        } else {
            nodeDisplay.registerHacker(this);
            const yOffset = nodeDisplay.getYOffset(this);
            return {xOffset: -OFFSET, yOffset: yOffset};
        }
    }

    repositionInNode(yOffset) {
        this.schedule.run(4, () => {
            this.moveStep(this.currentNodeDisplay, -OFFSET, yOffset, 4)
        });
    }

    animateMoveStepLine(fromNodeDisplay, toNodeDisplay, ticks) {
        const lineStartData = calcLineStart(fromNodeDisplay, toNodeDisplay, 0, 0);
        const lineEndData = calcLineWithOffset(fromNodeDisplay, toNodeDisplay, 0, 0, 0);

        const color = (this.hooked) ? COLOR_PATROLLER_LINE : COLOR_HACKER_LINE;

        const lineElement = new LineElement(lineStartData, color, this.canvas);
        lineElement.extendTo(lineEndData, ticks, fabric.util.ease.easeInOutSine);

        return lineElement
    }

    hackerProbeLayers(ticks) {
        this.schedule.run(ticks.start, () => {
            this.animateZoom(SCALE_SMALL, ticks.start);
        });
        this.schedule.run(ticks.end, () => {
            this.animateZoom(SCALE_NORMAL, ticks.end);
        });
    }

    hackerProbeConnections(nodeDisplay) {
        this.schedule.run(4, () => {
            this.moveStep(nodeDisplay, 0, 0, 4);
        });

        this.schedule.run(50, () => {
            this.animateZoom(SCALE_LARGE, 50);
            this.animateOpacity(0.6, 50)
        });
        this.schedule.run(45, () => {
            this.animateZoom(SCALE_NORMAL, 50);
            this.animateOpacity(1, 50)
        });
        this.schedule.run(4, () => {
            this.moveStep(nodeDisplay, OFFSET, OFFSET, 4);
        });
    }

    animateZoom(scale, time) {
        animate(this.canvas, this.hackerIcon, 'scaleX', scale, time);
        animate(this.canvas, this.hackerIcon, 'scaleY', scale, time);
    }

    animateOpacity(opacity, time, easing) {
        animate(this.canvas, this.hackerIcon, 'opacity', opacity, time, easing);
    }

    terminate() {
        this.schedule.terminate();
    }


    // this.schedule.run(0, () => {
    //     if (this.locked) {
    //         this.snapBackAndLock(this.currentNodeDisplay, nodeDisplay);
    //     }
    //     else {
    //         if (this.moveLineElement) {
    //             this.moveLineElement.disappearAndRemove(TICKS_HACKER_MOVE_END);
    //             this.moveLineElement = null;
    //         }
    //         if (this.you) {
    //             this.dispatch({type: HACKER_MOVE_ARRIVE, nodeId: nodeDisplay.id});
    //         }
    //     }
    // });
    // snapBackAndLock(snapBackToNodeDisplay, tempArrivedNodeDisplay) {
    //     this.moveStep(this.currentNodeDisplay, 0, 0, TICKS_HACKER_MOVE_MAIN);
    //
    //     if (!this.moveLineElement) {
    //         const lineEndData = calcLineWithOffset(snapBackToNodeDisplay, tempArrivedNodeDisplay, 0, 0, 0);
    //         this.moveLineElement = new LineElement(lineEndData, COLOR_HACKER_LINE, this.canvas);
    //         this.moveLineElement.setColor(COLOR_PATROLLER_LINE);
    //     }
    //
    //     const lineStartData = calcLineStart(snapBackToNodeDisplay, tempArrivedNodeDisplay, 0, 0);
    //     this.moveLineElement.extendTo(lineStartData, TICKS_HACKER_MOVE_MAIN, fabric.util.ease.easeInOutSine);
    //     this.schedule.wait(TICKS_HACKER_MOVE_MAIN);
    //     this.undoMoveStartAndCaptureComplete(snapBackToNodeDisplay);
    // }
    //
    //
    //
    // undoMoveStartAndCaptureComplete(nodeDisplay) {
    //     const {xOffset, yOffset} = this.processOffset(nodeDisplay);
    //     this.schedule.run(TICKS_HACKER_MOVE_START, () => {
    //         this.moveStep(this.currentNodeDisplay, xOffset, yOffset, TICKS_HACKER_MOVE_START);
    //     });
    //     this.schedule.run(4, () => {
    //         this.lockByPatrollerComplete();
    //     });
    // }


    lockByPatroller() {
        this.schedule.run(0, () => {
            this.lockIcon = new fabric.Rect({
                width: 35,
                height: 35,
                fill: COLOR_PATROLLER_LINE,
                left: this.hackerIcon.left,
                top: this.hackerIcon.top - 1,
                opacity: 0,
                hoverCursor: 'default',
                selectable: false,
            });
            this.lockIcon.rotate(45);
            this.canvas.add(this.lockIcon);
            this.canvas.bringToFront(this.lockIcon);
            this.canvas.bringToFront(this.hackerIcon);

            animate(this.canvas, this.lockIcon, "opacity", 1, 20);
        });
    }


}
