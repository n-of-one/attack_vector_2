import {fabric} from "fabric";
import {animate, calcLine, easeLinear} from "./CanvasUtils";
import {SERVER_TERMINAL_RECEIVE, TERMINAL_LOCK, TERMINAL_UNLOCK} from "../terminal/TerminalActions";

const APPEAR_TIME = 20;
const DISAPPEAR_TIME = 10;

export default class HackerDisplay {

    canvas = null;
    startNode = null;

    hackerIcon = null;
    hackerIdentifierIcon = null;
    hackerHider = null;
    lineIcon = null;
    labelIcon = null;

    schedule = null;
    hacker = null;
    you = false;
    startNodeDisplay = null;

    line = null;
    y = null;
    x = null;

    dispatch = null;

    constructor(canvas, schedule, startNodeDisplay, hacker, offset, you, dispatch) {
        this.canvas = canvas;
        this.schedule = schedule;
        this.hacker = hacker;
        this.startNodeDisplay = startNodeDisplay;
        this.dispatch = dispatch;

        this.x = offset;
        this.y = 810 - 20;

        const size = you ? 60 : 40;

        this.hackerIdentifierIcon = this.createHackerIcon(size, 0, this);
        this.canvas.add(this.hackerIdentifierIcon);
        animate(this.canvas, this.hackerIdentifierIcon, "opacity", 1, APPEAR_TIME);

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
        this.canvas.add(this.hackerHider);


        const hackerName = you ? "You" : hacker.userName;
        this.labelIcon = new fabric.Text(hackerName, {
            fill: "#f0ad4e",    // color-ok
            fontFamily: "courier",
            fontSize: 12,
            fontStyle: "normal", // "", "normal", "italic" or "oblique".
            // fontWeight: 10,
            left: this.x,
            top: this.y + 15,
            textAlign: "center", // "center", "right" or "justify".
            opacity: 0,
            selectable: false,
            hoverCursor: "default",
        });
        this.canvas.add(this.labelIcon);
        animate(this.canvas, this.labelIcon, "opacity", 1, APPEAR_TIME);

        const lineData = calcLine(this, startNodeDisplay);

        this.lineIcon = new fabric.Line(
            lineData.asArray(), {
                stroke: "#bb8",
                strokeWidth: 2,
                strokeDashArray: [4, 4],
                selectable: false,
                hoverCursor: 'default',
                opacity: 0
            });
        this.canvas.add(this.lineIcon);
        this.canvas.sendToBack(this.lineIcon);
        this.schedule.run(3, () => animate(this.canvas, this.lineIcon, "opacity", 0.5, 40));
    }

    createHackerIcon(size, opacity, position) {
        const image = document.getElementById(this.hacker.icon);

        const icon = new fabric.Image(image, {
            left: position.x,
            top: position.y,
            height: size,
            width: size,
            opacity: opacity,
            selectable: false,
            hoverCursor: "default",
        });

        return icon
    }

    size() {
        return 30;
    }

    move(newX) {
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
            animate(this.canvas, this.hackerIdentifierIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.lineIcon, "opacity", 0, DISAPPEAR_TIME);
            animate(this.canvas, this.labelIcon, "opacity", 0, DISAPPEAR_TIME);
        });
        this.schedule.run(0, () => {
            this.canvas.remove(this.hackerIdentifierIcon);
            this.canvas.remove(this.lineIcon);
            this.canvas.remove(this.labelIcon);
            this.canvas.remove(this.hackerHider);
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
        this.schedule.run(0, () => {
            this.echo(0, "[info]Persona established, hack started.");
            this.moveStep(this.startNodeDisplay, 20, 20, 5);
            animate(this.canvas, this.hackerIcon, 'opacity', 1, 5);
        });
    }


    startRunSlow() {
        this.dispatch({type: TERMINAL_LOCK, id: "main"});
        this.hackerIcon = this.createHackerIcon(60, 1, this);
        this.canvas.add(this.hackerIcon);
        this.canvas.sendToBack(this.hackerIcon);

        this.schedule.run(0, () => {
            this.moveStep(this.startNodeDisplay, 0, 0, 200, easeLinear);
            animate(this.canvas, this.hackerIcon, 'width', 40, 100);
            animate(this.canvas, this.hackerIcon, 'height', 40, 100);
        });

        const random = (max) => {
            return Math.floor(Math.random() * max);
        };
        const personaId = "" + random(10) + random(10) + random(10) + random(10) + random(10) + random(10) + '-' +
            random(10) + random(10) + random(10) + random(10) + '/' + random(10);

        this.echo(20, "");
        this.echo(20, "Persona v2.3 booting");
        this.echo(10, "- unique ID: " + personaId);
        this.schedule.run(0, () => {
            animate(this.canvas, this.hackerIcon, 'opacity', 0.3, 100, fabric.util.ease.easeOutSine);
            this.canvas.bringToFront(this.hackerIcon);
        });
        this.echo(10, "- Matching fingerprint with OS deamon");
        this.echo(10, "  - [ok]ok[/] Suppressing persona signature");
        this.echo(10, "  - [ok]ok[/] Connection bandwidth adjusted");
        this.echo(10, "  - [ok]ok[/] Content masked.");
        this.echo(30, "  - [ok]ok[/] Operating speed reduced to mimic OS deamon");
        this.echo(30, "  - [ok]ok[/] Network origin obfuscated ");
        this.schedule.run(0, () => {
            animate(this.canvas, this.hackerIcon, 'opacity', 1, 100);
        });
        this.echo(20, "- Persona creation [info]complete");
        this.echo(0, "");
        this.echo(80, "Entering node");
        this.schedule.run(0, () => {
            this.moveStep(this.startNodeDisplay, 20, 20, 20)
        });
        this.echo(0, "Persona accepted by node OS.");
        this.schedule.run(0, () => {
            this.dispatch({type: TERMINAL_UNLOCK, id: "main"});
        });
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


    // movePersona(payload) {
    //     var targetNodeId = payload.nodeId;
    //     var newNodeStatus = payload.newNodeStatus;
    //
    //     avatarMoveToNode = nodesById[targetNodeId];
    //
    //     this.schedule.run(4, function () {
    //         moveStep(currentNode, 0, 0, 200, hackerAvatar);
    //     });
    //     this.schedule.run(12, function () {
    //         moveStep(avatarMoveToNode, 0, 0, 800, hackerAvatar);
    //         currentNode = avatarMoveToNode;
    //     });
    //     this.schedule.run(4, function () {
    //         personaArrivesAtNode(avatarMoveToNode, newNodeStatus);
    //     });
    //
    //     this.schedule.run(0, function () {
    //         moveStep(avatarMoveToNode, 20, 20, 200, hackerAvatar);
    //     });
    // }
}
