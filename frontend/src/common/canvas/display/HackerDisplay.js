import {fabric} from "fabric";
import {animate, calcLine, calcLineStart, calcLineWithOffset, easeLinear, easeOutSine} from "../CanvasUtils";
import {SERVER_TERMINAL_RECEIVE, TERMINAL_LOCK, TERMINAL_UNLOCK} from "../../terminal/TerminalActions";
import {HACKER_MOVE_ARRIVE, HACKER_PROBED_CONNECTIONS, HACKER_PROBED_LAYERS} from "../../../hacker/run/model/HackActions";
import Schedule from "../../Schedule";
import {
    IDENTIFICATION_SIZE_LARGE,
    IDENTIFICATION_SIZE_NORMAL,
    OFFSET,
    COLOR_PATROLLER_LINE,
    SIZE_LARGE,
    SIZE_NORMAL,
    SIZE_SMALL,
    TICKS_HACKER_MOVE_END, TICKS_HACKER_MOVE_MAIN, TICKS_HACKER_MOVE_START, COLOR_HACKER_LINE
} from "./util/DisplayConstants";
import LineElement from "./util/LineElement";

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

    hackerIcon = null;
    hackerIdentifierIcon = null;
    hackerHider = null;
    startLineIcon = null;
    labelIcon = null;
    moveLineElement = null;

    hackerData = null;
    you = false;
    startNodeDisplay = null;
    currentNodeDisplay = null;
    y = null;
    x = null;
    inTransit = false;
    locked = false;

    constructor(canvas, startNodeDisplay, hackerData, offset, you, dispatch, currentNodeDisplay, targetNodeDisplay) {
        this.canvas = canvas;
        this.schedule = new Schedule();
        this.hackerData = hackerData;
        this.you = you;
        this.startNodeDisplay = startNodeDisplay;

        this.dispatch = dispatch;

        this.x = offset;
        this.y = 810 - 20;

        this.addHackerIdentificationIcons();

        this.schedule.run(3, () => {
            if (hackerData.hacking) {
                this.locked = hackerData.locked;

                // even if the hacker is in transit, we are not displaying it.
                // because it becomes a mess if the hacker is also locked during this transit.
                // Better to have the hacker icon jump from the current node to the target node.
                this.inTransit = false;
                this.addHackingHacker(currentNodeDisplay);

                animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, APPEAR_TIME);
                animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 40);
                animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, APPEAR_TIME);


            } else {
                this.currentNodeDisplay = startNodeDisplay;

                animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_SCANNING, APPEAR_TIME);
                animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_SCANNING, 40);
                animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_SCANNING, APPEAR_TIME);
            }
        });
    }

    addHackingHacker(currentNodeDisplay) {
        // even if the hacker is in transit, we are not displaying it.
        const nodeDisplay = currentNodeDisplay;

        this.currentNodeDisplay = nodeDisplay;

        const {xOffset, yOffset} = this.processOffset(nodeDisplay);
        this.hackerIcon = this.createHackerIcon(SIZE_NORMAL, 1, nodeDisplay, xOffset, yOffset);
        this.canvas.add(this.hackerIcon);

        if (this.locked) {
            this.lockByPatrollerComplete()
        }
    }

    createHackerIcon(size, opacity, position, offsetX, offsetY) {
        offsetX = (offsetX) ? offsetX : 0;
        offsetY = (offsetY) ? offsetY : 0;

        const image = document.getElementById(this.hackerData.icon);

        const icon = new fabric.Image(image, {
            left: position.x + offsetX,
            top: position.y + offsetY,
            height: size,
            width: size,
            opacity: opacity,
            selectable: false,
            hoverCursor: "default",
        });

        return icon
    }

    addHackerIdentificationIcons() {
        const size = this.you ? IDENTIFICATION_SIZE_LARGE : IDENTIFICATION_SIZE_NORMAL;
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
        const lineData = calcLine(this, this.startNodeDisplay);
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
            const lineData = calcLine(this, this.startNodeDisplay);
            animate(this.canvas, this.startLineIcon, null, lineData.asCoordinates(), APPEAR_TIME);
        });
    }

    disappear() {
        this.schedule.run(DISAPPEAR_TIME, () => {
            this.currentNodeDisplay.unregisterHacker(this);
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
        this.hackerIcon = this.createHackerIcon(SIZE_NORMAL, 1, this.startNodeDisplay);
        this.canvas.add(this.hackerIcon);
        this.canvas.bringToFront(this.hackerIcon);
        animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 10);
        animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, 10);
        animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, 10);

        this.schedule.run(0, () => {
            if (this.you) {
                this.echo(0, "[info]Persona established, hack started.");
                this.dispatch({type: HACKER_MOVE_ARRIVE, nodeId: this.startNodeDisplay.id});
            }
            animate(this.canvas, this.hackerIcon, 'opacity', 1, 5);
        });

    }


    startRunSlow() {
        this.hackerIcon = this.createHackerIcon(IDENTIFICATION_SIZE_LARGE, 0, this);
        this.canvas.add(this.hackerIcon);
        this.canvas.sendToBack(this.hackerIcon);

        this.schedule.run(0, () => {
            this.moveStep(this.startNodeDisplay, 0, 0, 200, easeLinear);
            this.animateZoom(SIZE_NORMAL, 100);
            this.animateOpacity(0.7, 20);

            animate(this.canvas, this.startLineIcon, "opacity", LINE_OPACITY_HACKING, 200);
            animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, 100);
            animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, 100);

        });

        if (this.you) {
            this.dispatch({type: TERMINAL_LOCK, id: "main"});
            const random = (max) => {
                return Math.floor(Math.random() * max);
            };
            const personaId = "" + random(10) + random(10) + random(10) + random(10) + random(10) + random(10) + '-' +
                random(10) + random(10) + random(10) + random(10) + '/' + random(10);

            this.echo(20, "");
            this.echo(20, "Persona v2.3 booting");
            this.echo(10, "- unique ID: " + personaId);
            this.schedule.run(0, () => {
                this.animateOpacity(0.1, 100, easeOutSine);
                this.canvas.bringToFront(this.hackerIcon);
            });
            this.echo(10, "- Matching fingerprint with OS deamon");
            this.echo(10, "  - [ok]ok[/] Suppressing persona signature");
            this.echo(10, "  - [ok]ok[/] Connection bandwidth adjusted");
            this.echo(10, "  - [ok]ok[/] Content masked.");
            this.echo(30, "  - [ok]ok[/] Operating speed reduced to mimic OS deamon");
            this.echo(30, "  - [ok]ok[/] Network origin obfuscated ");
            this.schedule.run(0, () => {
                this.animateOpacity(1, 100);
            });
            this.echo(20, "- Persona creation [info]complete");
            this.echo(0, "");
            this.echo(80, "Entering node");
            this.schedule.run(0, () => {
                this.dispatch({type: HACKER_MOVE_ARRIVE, nodeId: this.startNodeDisplay.id});
            });
            this.echo(0, "Persona accepted by node OS.");
            this.schedule.run(0, () => {
                this.dispatch({type: TERMINAL_UNLOCK, id: "main"});
            });
        } else {
            this.schedule.run(0, () => {
                this.canvas.bringToFront(this.hackerIcon);
            });
            this.schedule.wait(50);
            this.schedule.run(100, () => {
                this.animateOpacity(0.1, 100, easeOutSine);
                this.canvas.bringToFront(this.hackerIcon);
            });
            this.schedule.run(0, () => {
                this.animateOpacity(1, 100);
            });
        }
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

    moveStart(nodeDisplay) {
        this.inTransit = true;
        this.schedule.run(TICKS_HACKER_MOVE_START, () => {
            this.currentNodeDisplay.unregisterHacker(this);
            this.moveStep(this.currentNodeDisplay, 0, 0, TICKS_HACKER_MOVE_START);
        });
        this.schedule.run(TICKS_HACKER_MOVE_MAIN, () => {
            if (this.locked) {
                this.undoMoveStartAndCaptureComplete(nodeDisplay)
            } else {
                this.moveStep(nodeDisplay, 0, 0, TICKS_HACKER_MOVE_MAIN);
                this.moveLineElement = this.animateMoveStepLine(this.currentNodeDisplay, nodeDisplay);
            }
        });
        this.schedule.run(0, () => {
            if (this.locked) {
                this.snapBackAndLock(this.currentNodeDisplay, nodeDisplay);
            }
            else {
                if (this.moveLineElement) {
                    this.moveLineElement.disappearAndRemove(TICKS_HACKER_MOVE_END);
                    this.moveLineElement = null;
                }
                if (this.you) {
                    this.dispatch({type: HACKER_MOVE_ARRIVE, nodeId: nodeDisplay.id});
                }
            }
        });
    }

    undoMoveStartAndCaptureComplete(nodeDisplay) {
        const {xOffset, yOffset} = this.processOffset(nodeDisplay);
        this.schedule.run(TICKS_HACKER_MOVE_START, () => {
            this.moveStep(this.currentNodeDisplay, xOffset, yOffset, TICKS_HACKER_MOVE_START);
        });
        this.schedule.run(4, () => {
            this.lockByPatrollerComplete();
        });
    }

    snapBackAndLock(snapBackToNodeDisplay, tempArrivedNodeDisplay) {
        this.moveStep(this.currentNodeDisplay, 0, 0, TICKS_HACKER_MOVE_MAIN);

        if (!this.moveLineElement) {
            const lineEndData = calcLineWithOffset(snapBackToNodeDisplay, tempArrivedNodeDisplay, 0, 0, 0);
            this.moveLineElement = new LineElement(lineEndData, COLOR_HACKER_LINE, this.canvas);
            this.moveLineElement.setColor(COLOR_PATROLLER_LINE);
        }

        const lineStartData = calcLineStart(snapBackToNodeDisplay, tempArrivedNodeDisplay, 0, 0);
        this.moveLineElement.extendTo(lineStartData, TICKS_HACKER_MOVE_MAIN, fabric.util.ease.easeInOutSine);
        this.schedule.wait(TICKS_HACKER_MOVE_MAIN);
        this.undoMoveStartAndCaptureComplete(snapBackToNodeDisplay);
    }


    moveArrive(nodeDisplay) {
        this.currentNodeDisplay = nodeDisplay;
        this.schedule.run(4, () => {

            const {xOffset, yOffset} = this.processOffset(nodeDisplay);
            this.moveStep(nodeDisplay, xOffset, yOffset, 4);
        });
        this.inTransit = false;
        this.schedule.run(0, () => {
            if (this.locked) {
                this.schedule.wait(4);
                this.schedule.run(0, () => {
                    this.lockByPatrollerComplete();
                });
            }
        });
    }

    processOffset(nodeDisplay) {
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

    animateMoveStepLine(fromNodeDisplay, toNodeDisplay) {
        const lineStartData = calcLineStart(fromNodeDisplay, toNodeDisplay, 0, 0);
        const lineEndData = calcLineWithOffset(fromNodeDisplay, toNodeDisplay, 0, 0, 0);

        const lineElement = new LineElement(lineStartData, COLOR_HACKER_LINE, this.canvas);
        lineElement.extendTo(lineEndData, TICKS_HACKER_MOVE_MAIN, fabric.util.ease.easeInOutSine);

        return lineElement
    }



    hackerProbeLayers(nodeDisplay) {
        this.schedule.run(50, () => {
            this.animateZoom(SIZE_SMALL, 50);
        });
        this.schedule.run(45, () => {
            this.animateZoom(SIZE_NORMAL, 50);
        });
        this.schedule.run(5, () => {
            if (this.you) {
                this.dispatch({type: HACKER_PROBED_LAYERS, nodeId: nodeDisplay.id});
            }
        });
    }

    hackerProbeConnections(nodeDisplay) {
        this.schedule.run(4, () => {
            this.moveStep(nodeDisplay, 0, 0, 4);
        });

        this.schedule.run(50, () => {
            this.animateZoom(SIZE_LARGE, 50);
            this.animateOpacity(0.6, 50)
        });
        this.schedule.run(45, () => {
            this.animateZoom(SIZE_NORMAL, 50);
            this.animateOpacity(1, 50)
        });
        this.schedule.run(5, () => {
            if (this.you) {
                this.dispatch({type: HACKER_PROBED_CONNECTIONS, nodeId: nodeDisplay.id});
            }
        });
        this.schedule.run(4, () => {
            this.moveStep(nodeDisplay, OFFSET, OFFSET, 4);
        });
    }


    animateZoom(size, time) {
        animate(this.canvas, this.hackerIcon, 'width', size, time);
        animate(this.canvas, this.hackerIcon, 'height', size, time);
    }

    animateOpacity(opacity, time, easing) {
        animate(this.canvas, this.hackerIcon, 'opacity', opacity, time, easing);
    }

    terminate() {
        this.schedule.terminate();
    }

    lockByPatrollerStart() {
        this.locked = true;
        if (this.inTransit) {
            if (this.moveLineElement) {
                this.moveLineElement.setColor(COLOR_PATROLLER_LINE);
            }
            // Wait for the the move animation to complete. At that point we will detect we are locked, and trigger lockByPatrollerComplete().
        } else {
            this.lockByPatrollerComplete();
        }
    }

    lockByPatrollerComplete() {
        this.lockIcon = new fabric.Rect({
            width: 34,
            height: 34,
            fill: COLOR_PATROLLER_LINE,
            left: this.hackerIcon.left,
            top: this.hackerIcon.top -1,
            opacity: 0,
            hoverCursor: 'default',
            selectable: false,
        });
        this.lockIcon.rotate(45);
        this.canvas.add(this.lockIcon);
        this.canvas.bringToFront(this.lockIcon);
        this.canvas.bringToFront(this.hackerIcon);

        animate(this.canvas, this.lockIcon, "opacity", 1, 20);
    }


}
