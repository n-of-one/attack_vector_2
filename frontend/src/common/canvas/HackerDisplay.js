import {fabric} from "fabric";
import {animate, calcLine, easeLinear, easeOutSine} from "./CanvasUtils";
import {SERVER_TERMINAL_RECEIVE, TERMINAL_LOCK, TERMINAL_UNLOCK} from "../terminal/TerminalActions";
import {HACKER_MOVE_ARRIVE, HACKER_PROBED_CONNECTIONS, HACKER_PROBED_SERVICES} from "../../hacker/run/model/HackActions";
import Schedule from "../Schedule";

const APPEAR_TIME = 20;
const DISAPPEAR_TIME = 10;

const SIZE_NORMAL = 40;
const SIZE_SMALL = 20;
const SIZE_LARGE = 100;

const OFFSET = 20;


const IDENTIFIER_OPACITY_SCANNING = 0.1;
const IDENTIFIER_OPACITY_HACKING = 0.5;

const LINE_OPACITY_SCANNING = 0.5;
const LINE_OPACITY_HACKING = 1;



export default class HackerDisplay {

    canvas = null;

    hackerIcon = null;
    hackerIdentifierIcon = null;
    hackerHider = null;
    lineIcon = null;
    labelIcon = null;

    schedule = null;
    hacker = null;
    you = false;
    startNodeDisplay = null;
    currentNodeDisplay = null;

    line = null;
    y = null;
    x = null;

    dispatch = null;

    constructor(canvas, startNodeDisplay, hacker, offset, you, dispatch, currentNodeDisplay) {
        this.canvas = canvas;
        this.schedule = new Schedule();
        this.hacker = hacker;
        this.you = you;
        this.startNodeDisplay = startNodeDisplay;
        this.currentNodeDisplay = (currentNodeDisplay) ? currentNodeDisplay : startNodeDisplay;

        this.dispatch = dispatch;

        this.x = offset;
        this.y = 810 - 20;

        this.addHackerIdentificationIcons();

        this.schedule.run(3, () => {
            if (hacker.hacking) {
                this.addHackerIcon(hacker, currentNodeDisplay);

                animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_HACKING, APPEAR_TIME);
                animate(this.canvas, this.lineIcon, "opacity", LINE_OPACITY_HACKING, 40);
                animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_HACKING, APPEAR_TIME);
            }
            else {
                animate(this.canvas, this.hackerIdentifierIcon, "opacity", IDENTIFIER_OPACITY_SCANNING, APPEAR_TIME);
                animate(this.canvas, this.lineIcon, "opacity", LINE_OPACITY_SCANNING, 40);
                animate(this.canvas, this.labelIcon, "opacity", LINE_OPACITY_SCANNING, APPEAR_TIME);
            }
        });
    }

    addHackerIcon(hacker, currentNodeDisplay) {
        const opacity = (hacker.inTransit) ? 0 : 1;
        this.notVisible = hacker.inTransit;

        const {xOffset, yOffset} = this.processOffset(currentNodeDisplay);
        this.hackerIcon = this.createHackerIcon(SIZE_NORMAL, opacity, currentNodeDisplay, xOffset, yOffset);
        this.canvas.add(this.hackerIcon);
    }

    createHackerIcon(size, opacity, position, offsetX, offsetY) {
        offsetX = (offsetX) ? offsetX : 0;
        offsetY = (offsetY) ? offsetY : 0;

        const image = document.getElementById(this.hacker.icon);

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
        const size = this.you ? 60 : 40;
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
        const hackerName = this.you ? "You" : this.hacker.userName;
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
        this.lineIcon = new fabric.Line(
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
        this.canvas.add(this.lineIcon);
        this.canvas.sendToBack(this.lineIcon);

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
            animate(this.canvas, this.lineIcon, null, lineData.asCoordinates(), APPEAR_TIME);
        });
    }

    disappear() {
        this.schedule.run(DISAPPEAR_TIME, () => {
            this.currentNodeDisplay.unregisterHacker(this);
            animate(this.canvas, this.hackerIdentifierIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.lineIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.labelIcon, "opacity", 0, DISAPPEAR_TIME);
            if (this.hackerIcon) {
                animate(this.canvas, this.hackerIcon, "opacity", 0, DISAPPEAR_TIME);
            }
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.hackerIdentifierIcon);
            this.canvas.remove(this.lineIcon);
            this.canvas.remove(this.labelIcon);
            this.canvas.remove(this.hackerHider);
            if (this.hackerIcon) {
                this.canvas.remove(this.hackerIcon);
            }
        });
    }

    startRun(quick) {
        if (quick) {
            this.startRunQuick()
        }
        else {
            this.startRunSlow()
        }
    }

    startRunQuick() {
        this.hackerIcon = this.createHackerIcon(40, 1, this.startNodeDisplay);
        this.canvas.add(this.hackerIcon);
        this.canvas.bringToFront(this.hackerIcon);
        animate(this.canvas, this.lineIcon, "opacity", LINE_OPACITY_HACKING, 10);
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
        this.hackerIcon = this.createHackerIcon(60, 0, this);
        this.canvas.add(this.hackerIcon);
        this.canvas.sendToBack(this.hackerIcon);

        this.schedule.run(0, () => {
            this.moveStep(this.startNodeDisplay, 0, 0, 200, easeLinear);
            this.animateZoom(SIZE_NORMAL, 100);
            this.animateOpacity(0.7, 20);

            animate(this.canvas, this.lineIcon, "opacity", LINE_OPACITY_HACKING, 200);
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
        }
        else {
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
        this.schedule.run(4, () => {
            this.currentNodeDisplay.unregisterHacker(this);
            this.moveStep(this.currentNodeDisplay, 0, 0, 4);
        });
        this.schedule.run(16, () => {
            this.moveStep(nodeDisplay, 0, 0, 16);
        });
        if (this.you) {
            this.schedule.run(0, () => {
                this.dispatch({type: HACKER_MOVE_ARRIVE, nodeId: nodeDisplay.id});
            });
        }
    }

    moveArrive(nodeDisplay) {
        this.currentNodeDisplay = nodeDisplay;
        this.schedule.run(4, () => {
            this.appearIfNotVisible(nodeDisplay);

            const {xOffset, yOffset} = this.processOffset(nodeDisplay);
            this.moveStep(nodeDisplay, xOffset, yOffset, 4);
        });
    }

    processOffset(nodeDisplay) {
        if (this.you) {
            return {xOffset: OFFSET, yOffset: OFFSET};
        }
        else {
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

    hackerProbeServices(nodeDisplay) {
        this.schedule.run(50, () => {
            this.appearIfNotVisible(nodeDisplay);
            this.animateZoom(SIZE_SMALL, 50);
        });
        this.schedule.run(45, () => {
            this.animateZoom(SIZE_NORMAL, 50);
        });
        this.schedule.run(5, () => {
            if (this.you) {
                this.dispatch({type: HACKER_PROBED_SERVICES, nodeId: nodeDisplay.id});
            }
        });
    }

    hackerProbeConnections(nodeDisplay) {
        this.schedule.run(4, () => {
            this.appearIfNotVisible(nodeDisplay);
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

    /*
    When Hacker A enters a run that has hacker B who is in a move, the icon for hacker B is hidden (opacity 0)
    until Hacker B performs an action at a node. Then it appears.
     */
    appearIfNotVisible(nodeDisplay) {
        if (this.notVisible) {
            this.notVisible = false;
            this.hackerIcon.set("top", nodeDisplay.y);
            this.hackerIcon.set("left", nodeDisplay.x);
            this.animateOpacity(1, 4);
        }
    }

    animateZoom(size, time) {
        animate(this.canvas, this.hackerIcon, 'width', size, time);
        animate(this.canvas, this.hackerIcon, 'height', size, time);
    }

    animateOpacity(opacity, time, easing) {
        animate(this.canvas, this.hackerIcon, 'opacity', opacity, time, easing);
    }
}
